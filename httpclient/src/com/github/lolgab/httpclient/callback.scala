package com.github.lolgab.httpclient

object callback {
  def get(url: String)(callback: Response => Unit): Unit = {
    internal.CurlImpl.get(url)(callback)
  }
}
