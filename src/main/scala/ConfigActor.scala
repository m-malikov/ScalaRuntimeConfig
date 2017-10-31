import akka.actor.{Actor, ActorRef, Props}

import scala.collection.immutable.List

object ConfigActor {
  case class Update()
  case class AddDependent(id: Int)

  def props(id: Int): Props = Props(new ConfigActor(id))
}

class ConfigActor(id: Int) extends Actor {
  import ConfigActor._

  private var dependents: List[ActorRef] = Nil

  override def receive: Receive = {
    case Update =>
      // Reload service here
      println(s"Updated config #$id")
      dependents.foreach((actorRef: ActorRef) => actorRef ! Update)
    case AddDependent(dependentId) =>
      // Adding depending
      println(s"Adding dependent #$dependentId to config #$id")
      val dependent = context.actorOf(ConfigActor.props(dependentId))
      dependents = dependents.+:(dependent)
      sender() ! dependent
  }
}
