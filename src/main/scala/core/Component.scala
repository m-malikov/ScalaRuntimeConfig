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
  private val _system = ActorSystem("runtimeConfig")
  private implicit val _timeout: Timeout = Timeout(5 seconds)
  private var _components: Map[Int, Component] = new HashMap[Int, Component]()
  def terminateSystem(){ _system.terminate() }

  def components = _components
}

class Component(var onReload: () => Unit, var onChange: String => Unit) {
  import Component._
  import _system.dispatcher

  private var _actor: Future[ActorRef] = _

  val id = _components.size
  _components = _components.+((id, this))


  def hasDependent(dependent: Component) = {
    if (_actor == null) _actor = Future(_system.actorOf(ConfigActor.props(onReload, onChange)))
    val dependentActor = _actor.flatMap(ask(_, AddDependent(dependent.onReload, dependent.onChange)).mapTo[ActorRef])
    dependent._actor = dependentActor
    this
  }


  def reload = if (_actor != null) _actor.foreach(_ ! Reload) else onReload()

  def changeTo(config: String) = if (_actor != null) _actor.foreach(_ ! Change(config)) else onChange(config)
}
