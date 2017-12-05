package server

import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import core.ComponentSystem
import org.scalatest.{Matchers, WordSpec}

class RestServiceTest extends WordSpec with Matchers with ScalatestRouteTest with RestService {
  implicit val componentSystem: ComponentSystem = new ComponentSystem("tests")

  "Server API" should {
    "respond with html on a GET request" in {
      Get() ~> route ~> check {
        println(responseAs[String])
        status shouldEqual StatusCodes.OK
      }
    }
  }
}
