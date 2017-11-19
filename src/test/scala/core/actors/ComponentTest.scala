package core.actors

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import core.Component
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class ComponentTest extends TestKit(ActorSystem("compomentTest")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A Component" must {
    "return given value" in {
      val component = new Component("testComponent", () => "test value", () => {}, (c) => {})
      Await.result(component.getValue, Duration("1 second"))  shouldBe "test value"
    }

    ""
  }

}
