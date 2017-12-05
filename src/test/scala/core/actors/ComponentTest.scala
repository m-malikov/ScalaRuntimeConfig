package core.actors

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import components.TestComponent
import core.ComponentSystem
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class ComponentTest extends TestKit(ActorSystem("componentTest")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
  private implicit val componentSystem: ComponentSystem = new ComponentSystem("tests")
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A Component" must {
    "return given value" in {
      val component = new TestComponent("testComponent", "test value")
      Await.result(component.value, Duration("1 second")) shouldBe "test value"
    }

  }

}
