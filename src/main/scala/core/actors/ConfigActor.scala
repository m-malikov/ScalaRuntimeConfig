package core.actors

import akka.actor.{Actor, ActorRef, Props}

import scala.collection.immutable.List

object ConfigActor {
  case class Reload()
  case class Change(config: String)
  case class AddDependent(onReload: () => Unit, onChange: String => Unit)

  def props(onReload: () => Unit, onChange: String => Unit): Props = Props(new ConfigActor(onReload, onChange))
}

class ConfigActor(onReload: () => Unit, onChange: String => Unit) extends Actor {
  import ConfigActor._

  private var dependents: List[ActorRef] = Nil

  override def receive: Receive = {
    case Reload =>
      // Reload service here
      onReload()
      dependents.foreach(_ ! Reload)

    case Change(config) =>
      // Change config here
      onChange(config)
      dependents.foreach(_ ! Reload)

    case AddDependent(onDependentUpdate, onDependentChange) =>
      // Adding depending
      val dependent = context.actorOf(ConfigActor.props(onDependentUpdate, onDependentChange))
      dependents = dependents.+:(dependent)
      sender() ! dependent
  }
}
