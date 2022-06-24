package httpclient

import com.github.lolgab.httpclient._
import utest._
import scala.concurrent.ExecutionContext.Implicits.global

object HttpClientTests extends TestSuite {
  val tests = Tests {
    test("get request") {      
      Request().method(Method.GET).url("http://httpbin.org/get").future().map { response =>
        response.code ==> 200
        assert(response.body.contains(""""url": "http://httpbin.org/get""""))
      }
    }
    test("post request") {
      Request().method(Method.POST).url("http://httpbin.org/post").future().map { response =>
        response.code ==> 200
        assert(response.body.contains(""""url": "http://httpbin.org/post""""))
      }
    }
    test("put request") {
      Request().method(Method.PUT).url("http://httpbin.org/put").future().map { response =>
        response.code ==> 200
        assert(response.body.contains(""""url": "http://httpbin.org/put""""))
      }
    }
  }
}
