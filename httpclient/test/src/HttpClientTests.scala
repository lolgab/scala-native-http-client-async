package httpclient

import com.github.lolgab.httpclient.future
import utest._
import scala.concurrent.ExecutionContext.Implicits.global

object HttpClientTests extends TestSuite {
  val tests = Tests {
    test("get request") {
      future.get("http://httpbin.org/get").map { response =>
        response.code ==> 200
        assert(response.body.contains(""""url": "http://httpbin.org/get""""))
        assert(response.body.contains("""{
          |  "args": {}, 
          |  "headers": {""".stripMargin))
      }
    }
  }
}
