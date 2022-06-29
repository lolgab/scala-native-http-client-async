package com.github.lolgab.httpclient.internal

import scala.scalanative.unsafe._

@link("curl")
@extern private [httpclient] object CApi {
  type CurlBuffer = CStruct2[CString, CSize]
  type CurlOption = Int
  type CurlRequest = CStruct4[Ptr[Byte], Long, Long, Int]
  type CurlMessage = CStruct3[Int, Ptr[Byte], Ptr[Byte]]

  type CurlDataCallback = CFuncPtr4[Ptr[Byte], CSize, CSize, Ptr[CurlBuffer], CSize]
  type CurlSocketCallback =
    CFuncPtr5[Ptr[Byte], CInt, CInt, Ptr[Byte], Ptr[Byte], CInt]
  type CurlTimerCallback = CFuncPtr3[Ptr[Byte], Long, Ptr[Byte], CInt]

  @name("scalanative_CURL_GLOBAL_ALL")
  val CURL_GLOBAL_ALL: CInt = extern
  @name("scalanative_CURLINFO_PRIVATE")
  val CURLINFO_PRIVATE: CInt = extern
  @name("scalanative_CURLINFO_RESPONSE_CODE")
  val CURLINFO_RESPONSE_CODE: CInt = extern
  @name("scalanative_CURLMSG_DONE")
  val CURLMSG_DONE: CInt = extern
  @name("scalanative_CURLMOPT_TIMERFUNCTION")
  val CURLMOPT_TIMERFUNCTION: CInt = extern
  @name("scalanative_CURLMOPT_SOCKETFUNCTION")
  val CURLMOPT_SOCKETFUNCTION: CInt = extern
  @name("scalanative_CURLOPT_COPYPOSTFIELDS")
  val CURLOPT_COPYPOSTFIELDS: CInt = extern
  @name("scalanative_CURLOPT_CUSTOMREQUEST")
  val CURLOPT_CUSTOMREQUEST: CInt = extern
  @name("scalanative_CURLOPT_HTTPGET")
  val CURLOPT_HTTPGET: CInt = extern
  @name("scalanative_CURLOPT_NOBODY")
  val CURLOPT_NOBODY: CInt = extern
  @name("scalanative_CURLOPT_WRITEDATA")
  val CURLOPT_WRITEDATA: CInt = extern
  @name("scalanative_CURLOPT_POST")
  val CURLOPT_POST: CInt = extern
  @name("scalanative_CURLOPT_PRIVATE")
  val CURLOPT_PRIVATE: CInt = extern
  @name("scalanative_CURLOPT_PUT")
  val CURLOPT_PUT: CInt = extern
  @name("scalanative_CURLOPT_URL")
  val CURLOPT_URL: CInt = extern
  @name("scalanative_CURLOPT_WRITEFUNCTION")
  val CURLOPT_WRITEFUNCTION: CInt = extern
  @name("scalanative_CURL_SOCKET_TIMEOUT")
  val CURL_SOCKET_TIMEOUT: CInt = extern
  @name("scalanative_CURL_POLL_IN")
  val CURL_POLL_IN: CInt = extern
  @name("scalanative_CURL_POLL_OUT")
  val CURL_POLL_OUT: CInt = extern
  @name("scalanative_CURL_POLL_INOUT")
  val CURL_POLL_INOUT: CInt = extern
  @name("scalanative_CURL_POLL_REMOVE")
  val CURL_POLL_REMOVE: CInt = extern
  @name("scalanative_CURL_CSELECT_IN")
  val CURL_CSELECT_IN: CInt = extern
  @name("scalanative_CURL_CSELECT_OUT")
  val CURL_CSELECT_OUT: CInt = extern

  def curl_global_init(flags: Long): Int = extern

  def curl_global_cleanup(): Unit = extern

  def curl_easy_init(): Ptr[Byte] = extern

  def curl_easy_cleanup(handle: Ptr[Byte]): Unit = extern

  def curl_easy_setopt(
      handle: Ptr[Byte],
      option: CInt,
      parameter: CVarArgList
  ): CInt = extern

  def curl_easy_setopt(
      handle: Ptr[Byte],
      option: CInt,
      parameter: Ptr[Byte]
  ): CInt =
    extern

  def curl_easy_setopt(
      handle: Ptr[Byte],
      option: CInt,
      parameter: CFuncPtr
  ): CInt =
    extern

  def curl_easy_setopt(
      handle: Ptr[Byte],
      option: CInt,
      parameter: Long
  ): CInt =
    extern

  def curl_easy_getinfo(
      handle: Ptr[Byte],
      info: CInt,
      parameter: Ptr[Byte]
  ): CInt =
    extern

  def curl_easy_perform(handle: Ptr[Byte]): CInt = extern

  def curl_multi_init(): Ptr[Byte] = extern

  def curl_multi_add_handle(multi: Ptr[Byte], easy: Ptr[Byte]): Int = extern

  def curl_multi_remove_handle(multi: Ptr[Byte], easy: Ptr[Byte]): Int = extern

  def curl_multi_setopt(
      multi: Ptr[Byte],
      option: CInt,
      parameter: Ptr[Byte]
  ): CInt = extern

  def curl_multi_setopt(
      multi: Ptr[Byte],
      option: CInt,
      parameter: CFuncPtr
  ): CInt = extern

  def curl_multi_assign(
      multi: Ptr[Byte],
      socket: Int,
      socket_data: Ptr[Byte]
  ): Int = extern

  def curl_multi_socket_action(
      multi: Ptr[Byte],
      socket: Int,
      events: Int,
      numhandles: Ptr[Int]
  ): Int = extern

  def curl_multi_info_read(
      multi: Ptr[Byte],
      message: Ptr[Int]
  ): Ptr[CurlMessage] =
    extern

  def curl_multi_perform(multi: Ptr[Byte], numhandles: Ptr[Int]): Int = extern

  def curl_multi_cleanup(multi: Ptr[Byte]): Int = extern

  type CurlSList = CStruct2[Ptr[Byte], CString]

  def curl_slist_append(
      slist: Ptr[CurlSList],
      string: CString
  ): Ptr[CurlSList] =
    extern

  def curl_slist_free_all(slist: Ptr[CurlSList]): Unit = extern

  def curl_easy_strerror(code: Int): CString = extern
}
object CApiOps {
  import CApi._
  implicit class CurlMessageOps(val ptr: Ptr[CurlMessage]) extends AnyVal {
    def msg: CInt = ptr._1
    def easy_handle: Ptr[Byte] = ptr._2
  }
}
