package server

import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}

class RestServiceTest extends WordSpec with Matchers with ScalatestRouteTest with RestService {
  "Server API" should {
    "respond with html on a GET request" in {
      Get() ~> route ~> check {
        println(responseAs[String])
        status shouldEqual StatusCodes.OK
      }
    }
  }
}
