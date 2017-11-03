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

class Component(private var _getConfig: () => String,
                private var _onReload: () => Unit,
                private var _onChange: String => Unit) {
  import Component._
  import _system.dispatcher

  private var _actor: Future[ActorRef] = _

  val id = _components.size
  _components = _components.+((id, this))


  def hasDependent(dependent: Component) = {
    if (_actor == null) _actor = Future(_system.actorOf(ConfigActor.props(_onReload, _onChange)))
    val dependentActor = _actor.flatMap(ask(_, AddDependent(dependent._onReload, dependent._onChange)).mapTo[ActorRef])
    dependent._actor = dependentActor
    this
  }


  def getConfig = _getConfig

  def reload = if (_actor != null) _actor.foreach(_ ! Reload) else _onReload()

  def changeTo(config: String) = if (_actor != null) _actor.foreach(_ ! Change(config)) else _onChange(config)
}
