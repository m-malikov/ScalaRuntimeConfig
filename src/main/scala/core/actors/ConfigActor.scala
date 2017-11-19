package core.actors

import akka.actor.{Actor, ActorRef, Props}

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContextExecutor, Future}

object ConfigActor {
  case class Value()
  case class Reload()
  case class Change(config: String)
  case class AddDependent(getValue: () => String,
                          onReload: () => Unit,
                          onChange: String => Unit)

  case class AddDependentActor(dependent: Future[ActorRef])

  def props(getValue: () => String,
            onReload: () => Unit,
            onChange: String => Unit): Props = Props(new ConfigActor(onReload, onChange, getValue))
}

class ConfigActor(onReload: () => Unit,
                  onChange: String => Unit,
                  getValue: () => String) extends Actor {
  import ConfigActor._
  implicit val ec: ExecutionContextExecutor = context.system.dispatcher

  private var dependents = new ListBuffer[ActorRef]

  override def receive: Receive = {
    case Reload =>
      // Reload service here
      onReload()
      dependents.foreach(_ ! Reload)

    case Change(value) =>
      // Change config here
      onChange(value)
      dependents.foreach(_ ! Reload)

    case AddDependent(getDependentValue, onDependentUpdate, onDependentChange) =>
      // Adding depending
      val dependent = context.actorOf(ConfigActor.props(getDependentValue, onDependentUpdate, onDependentChange))
      dependents += dependent
      sender() ! dependent

    case AddDependentActor(dependent) =>
      dependent.foreach(dependents += _)

    case Value =>
      val config: String = getValue()
      sender() ! config
  }
}
