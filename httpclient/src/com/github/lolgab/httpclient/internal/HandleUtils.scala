package com.github.lolgab.httpclient.internal

import scala.scalanative.runtime._
import scala.scalanative.runtime.Intrinsics._
import scala.scalanative.unsafe.{Ptr, stackalloc}
import scala.scalanative.libc.stdlib
import CApi._

private[internal] object HandleUtils {
  private val references = new java.util.IdentityHashMap[Object, Int]()

  @inline def getData[T <: Object](handle: Ptr[Byte]): T = {
    // data is the first member of uv_loop_t
    val ptrOfPtr = stackalloc[Ptr[Byte]]()
    curl_easy_getinfo(handle, CURLINFO_PRIVATE, ptrOfPtr.asInstanceOf[Ptr[Byte]])
    val dataPtr = !ptrOfPtr
    if (dataPtr == null) null.asInstanceOf[T]
    else {
      val rawptr = toRawPtr(dataPtr)
      castRawPtrToObject(rawptr).asInstanceOf[T]
    }
  }
  @inline def setData(handle: Ptr[Byte], obj: Object): Unit = {
    if (obj != null) {
      references.put(obj, references.get(obj) + 1)
      val rawptr = castObjectToRawPtr(obj)
      curl_easy_setopt(handle, CURLOPT_PRIVATE, fromRawPtr[Byte](rawptr))
    } else {
      curl_easy_setopt(handle, CURLOPT_PRIVATE, null.asInstanceOf[Ptr[Byte]])
    }
  }
  @inline def close(handle: Ptr[Byte]): Unit = {
    if (getData(handle) != null) {
      val data = getData[Object](handle)
      val current = references.get(data)
      if (current > 1) references.put(data, current - 1)
      else references.remove(data)
      setData(handle, null)
    }
  }
}
