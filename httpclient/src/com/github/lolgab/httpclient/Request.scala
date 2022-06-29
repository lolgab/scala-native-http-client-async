package com.github.lolgab.httpclient

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
import com.github.lolgab.httpclient._
import com.github.lolgab.httpclient.internal._
import scala.annotation.tailrec
import scala.concurrent._

class Request private (handle: Ptr[Byte]) {
  import CurlImpl._
  import CApi._
  import CApiOps._
  private [httpclient] var callback: Response => Unit = null
  private [httpclient] var memory: Ptr[Memory] = malloc(sizeof[Memory]).asInstanceOf[Ptr[Memory]]
  private [httpclient] var headersList: Ptr[CurlSList] = null
  memory._1 = malloc(0.toULong)
  memory._2 = 0.toULong
  curl_easy_setopt(
    handle,
    CURLOPT_WRITEDATA,
    memory.asInstanceOf[Ptr[Byte]]
  )
  curl_easy_setopt(
    handle,
    CURLOPT_WRITEFUNCTION,
    writeMemoryCallback
  )
  HandleUtils.setData(handle, this)
  def method(value: Method): Request = Zone { implicit z =>
    curl_easy_setopt(
      handle,
      CURLOPT_CUSTOMREQUEST,
      toCString(value.name)
    )
    this
  }
  def url(value: String): Request = {
    Zone { implicit z => curl_easy_setopt(handle, CURLOPT_URL, toCString(value)) }
    this
  }
  def body(value: String): Request = Zone { implicit z =>
    curl_easy_setopt(
      handle,
      CURLOPT_COPYPOSTFIELDS,
      toCString(value)
    )
    this
  }
  def header(value: String): Request = Zone { implicit z =>
    headersList = curl_slist_append(headersList, toCString(value))
    this
  }
  private def setCallback(value: Response => Unit): Request = {
    this.callback = value
    this
  }
  private def perform(): Unit = {
    curl_easy_setopt(
      handle,
      CURLOPT_HTTPHEADER,
      headersList.asInstanceOf[Ptr[Byte]]
    )
    curl_multi_add_handle(curlHandle, handle)
  }
  def future(): Future[Response] = {
    val p = Promise[Response]()
    setCallback(p.success(_))
    perform()
    p.future
  }
  def callback(value: Response => Unit): Unit = {
    setCallback(value)
    perform()
  }
}
object Request {
  import CApi._
  def apply(): Request = new Request(curl_easy_init())
}
