package com.github.lolgab.httpclient.internal

import scala.scalanative.runtime._
import scala.scalanative.runtime.Intrinsics._
import scala.scalanative.unsafe.{Ptr, stackalloc}
import scala.scalanative.libc.stdlib
import CApi._

private[httpclient] object HandleUtils {
  private val references = new java.util.IdentityHashMap[Object, Int]()

  @inline def getData[T <: Object](handle: Ptr[Byte]): T = {
    val ptrOfPtr = stackalloc[Ptr[Byte]]()
    curl_easy_getinfo(
      handle,
      CURLINFO_PRIVATE,
      ptrOfPtr.asInstanceOf[Ptr[Byte]]
    )
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
    }
  }
  @inline def unref(obj: Object): Unit = {
    if (obj != null) {
      val current = references.get(obj)
      if (current > 1) references.put(obj, current - 1)
      else references.remove(obj)
    }
  }
}
