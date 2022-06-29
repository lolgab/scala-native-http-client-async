package com.github.lolgab.httpclient.internal

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.collection.mutable
import scala.concurrent.duration._
import scala.scalanative.libc.stdlib._
import scala.scalanative.libc.string._
import scala.scalanative.runtime.{
  ByteArray,
  Intrinsics,
  LongArray,
  MemoryPool,
  fromRawPtr,
  toRawPtr
}
import scala.scalanative.loop._
import scala.scalanative.loop.LibUV._
import scala.scalanative.loop.LibUVConstants._
import com.github.lolgab.httpclient._
import scala.annotation.tailrec

private [httpclient] object CurlImpl {
  import CApi._
  import CApiOps._

  if (curl_global_init(CURL_GLOBAL_ALL) != 0) {
    throw new Error("Could not init curl")
  }

  private val timerMemory = new Array[Byte](uv_handle_size(UV_TIMER_T).toInt)
  val timerHandle = timerMemory.asInstanceOf[ByteArray].at(0)
  uv_timer_init(EventLoop.loop, timerHandle)

  val curlHandle = curl_multi_init()

  private def curlPerform(rwResult: RWResult, sockfd: Int) = {
    val runningHandles = stackalloc[CInt]()
    var flags = 0

    if (rwResult.readable)
      flags |= CURL_CSELECT_IN;
    if (rwResult.writable)
      flags |= CURL_CSELECT_OUT;

    curl_multi_socket_action(
      curlHandle,
      sockfd,
      flags,
      runningHandles
    )

    checkMultiInfo()
  }

  class CurlContext(val poll: Poll, val sockfd: Int)
  object CurlContext {
    private val contexts = mutable.Set[CurlContext]()
    def apply(sockfd: Int): CurlContext = {
      val res = new CurlContext(Poll(sockfd), sockfd)
      // Hold the reference to avoid garbage collection
      contexts += res
      res
    }
    def unref(context: CurlContext) = contexts -= context
  }

  val handleSocketCFuncPtr: CurlSocketCallback = (
      easy: Ptr[Byte],
      sockfd: Int,
      action: Int,
      userp: Ptr[Byte],
      socketp: Ptr[Byte]
  ) => {
    val context =
      Intrinsics.castRawPtrToObject(toRawPtr(socketp)).asInstanceOf[CurlContext]
    handleSocket(easy, sockfd, action, context)
  }

  def handleSocket(
      easy: Ptr[Byte],
      sockfd: Int,
      action: Int,
      socketp: CurlContext
  ): Int = {
    action match {
      case CURL_POLL_IN | CURL_POLL_OUT | CURL_POLL_INOUT =>
        val curlContext = if (socketp == null) {
          CurlContext(sockfd)
        } else {
          socketp
        }
        curl_multi_assign(
          curlHandle,
          sockfd,
          fromRawPtr[Byte](Intrinsics.castObjectToRawPtr(curlContext))
        )
        curlContext.poll
          .start((action & CURL_POLL_IN) != 0, (action & CURL_POLL_OUT) != 0) {
            rwResult => curlPerform(rwResult, sockfd)
          }
      case CURL_POLL_REMOVE =>
        if (socketp != null) {
          socketp.poll.stop()
          CurlContext.unref(socketp)
          curl_multi_assign(curlHandle, sockfd, null)
        }
      case other =>
        throw new java.lang.RuntimeException(
          s"Action code not supported $other"
        )
    }
    0
  }
  curl_multi_setopt(curlHandle, CURLMOPT_SOCKETFUNCTION, handleSocketCFuncPtr)
  val startTimeout: CurlTimerCallback =
    (multi: Ptr[Byte], timeout_ms: CLong, userp: Ptr[Byte]) => {
      if (timeout_ms < 0) uv_timer_stop(timerHandle)
      else {
        val newTimeout = if (timeout_ms == 0) 1 else timeout_ms.toLong
        uv_timer_start(timerHandle, (_: Ptr[Byte]) => {
          val running_handles = stackalloc[CInt]()
            curl_multi_socket_action(
              curlHandle,
              CURL_SOCKET_TIMEOUT,
              0,
              running_handles
            )
          checkMultiInfo()
        }, newTimeout, 0)
      }
      0
    }
  curl_multi_setopt(curlHandle, CURLMOPT_TIMERFUNCTION, startTimeout)

  val writeMemoryCallback: CurlDataCallback =
    (ptr: Ptr[Byte], size: CSize, nmemb: CSize, data: Ptr[CurlBuffer]) => {
      val index: CSize = (!data)._2
      val increment: CSize = size * nmemb
      (!data)._2 = (!data)._2 + increment
      (!data)._1 = realloc((!data)._1, (!data)._2 + 1.toUInt)
      memcpy((!data)._1 + index, ptr, increment)
      !(!data)._1.+((!data)._2) = 0.toByte
      size * nmemb
    }

  type Memory = CStruct2[Ptr[Byte], CSize]
  case class CurlData(

  )
  def checkMultiInfo(): Unit = {
    @tailrec
    def loop(): Unit = {
      val pending = stackalloc[CInt]()
      val message = curl_multi_info_read(curlHandle, pending)
      if (message != null) {
        if (message.msg == CURLMSG_DONE) {
          val easyHandle = message.easy_handle
          val responseCode = stackalloc[CLong]()
          curl_easy_getinfo(
            easyHandle,
            CURLINFO_RESPONSE_CODE,
            responseCode.asInstanceOf[Ptr[Byte]]
          )
          val request = HandleUtils.getData[Request](easyHandle)
          val response = Response(
            code = (!responseCode).toInt,
            body = StringUtils
              .fromCStringAndSize(request.memory._1, request.memory._2.toInt)
          )
          try {
            request.callback(response)
          } finally {
            free(request.memory._1)
            free(request.memory.asInstanceOf[Ptr[Byte]])
            curl_slist_free_all(request.headersList)
            curl_multi_remove_handle(curlHandle, easyHandle)
            curl_easy_cleanup(easyHandle)
          }
        }
        loop()
      }
    }
    loop()
  }
}
