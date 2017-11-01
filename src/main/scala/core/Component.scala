package core

import core.actors.ConfigActor.{AddDependent, Change, Reload}
import core.actors.ConfigActor

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.collection.immutable.HashMap

object Component {
  private val system = ActorSystem("runtimeConfig")
  implicit val timeout: Timeout = Timeout(5 seconds)
  private var components: Map[String, Component] = new HashMap[String, Component]()

  def terminateSystem(){ system.terminate() }

  def getComponents = components
}

class Component(var id: String, var onReload: () => Unit, var onChange: String => Unit) {
  import Component._
  import system.dispatcher

  components = components.+((id, this))

  private var actor: Future[ActorRef] = _

  def hasDependent(dependent: Component) = {
    if (actor == null) actor = Future(system.actorOf(ConfigActor.props(onReload, onChange)))
    val dependentActor = actor.flatMap(ask(_, AddDependent(dependent.onReload, dependent.onChange)).mapTo[ActorRef])
    dependent.actor = dependentActor
    this
  }

  def update = actor.foreach(_ ! Reload)

  def changeTo(config: String) = actor.foreach(_ ! Change(config))
}
