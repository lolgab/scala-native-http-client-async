package com.github.lolgab.httpclient

import scala.concurrent._

object future {
  def get(url: String): Future[Response] = {
    val promise = Promise[Response]()
    internal.CurlImpl.get(url)(response => promise.success(response))
    promise.future
  }
}
