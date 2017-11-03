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
  /** Actor system for component actors. */
  private val _system = ActorSystem("runtimeConfig")

  /** Timeout for Futures. */
  private implicit val _timeout: Timeout = Timeout(5 seconds)

  /** HashMap of application components. Stores all created Components as pairs id -> component.
    * @see Component#_id
    */
  private var _components: Map[Int, Component] = new HashMap[Int, Component]()

  /**
    * Terminates runtime config system. Call this method for stopping the application.
    */
  def terminateSystem(){ _system.terminate() }

  /**
    * Getter for #_components.
    *
    * @return HashMap containing all created Components as pairs id -> component.
    */
  def components = _components
}


/**
  * Basic class corresponding application component having some configuration, optionally dependency
  * components and dependent components. If one component is depending on other, the first one must be reloaded
  * when the second one's configuration is changed. Each component has an akka actor reliable for notifying
  * dependent components when it's being reloaded or its config is changed. Provides methods for
  * getting configuration, reloading component and changing service's settings.
  *
  * @param _getConfig function getting component configuration as string.
  * @param _onReload function called to reload the component.
  * @param _onChange function changing component config to passed string.
  */
class Component(private var _getConfig: () => String,
                private var _onReload: () => Unit,
                private var _onChange: String => Unit) {
  import Component._

  // Context for futures.
  import _system.dispatcher

  /** Akka actor reliable for this component */
  private var _actor: Future[ActorRef] = _

  /** Identification number of this component. Initialized with component order number. */
  val id = _components.size
  _components = _components.+((id, this))


  /**
    * DSL method for defining dependent components for current one. This method can be used in chains for
    * specifying dependent actors for first actor in the chain.
    *
    * @example `component1 hasDependent component11 has dependent component12` this means that
    *         component11 and component12 depend on component1.
    *
    *
    * @param dependent depending component.
    * @return current component.
    */
  def hasDependent(dependent: Component) = {
    if (_actor == null) _actor = Future(_system.actorOf(ConfigActor.props(_onReload, _onChange)))
    val dependentActor = _actor.flatMap(ask(_, AddDependent(dependent._onReload, dependent._onChange)).mapTo[ActorRef])
    dependent._actor = dependentActor
    this
  }


  // valentiay: I suppose this is not thread safe and we must put getting config inside the actor
  /**
    * Config getter.
    *
    * @return configuration of this component as String.
    */
  def getConfig = _getConfig

  // valentiay: I suppose this is not thread safe and we must put
  // reloading service inside the actor for the 'else' case also.
  /**
    * Reloads this component and all dependent.
    */
  def reload = if (_actor != null) _actor.foreach(_ ! Reload) else _onReload()

  // valentiay: I suppose this is not thread safe and we must put
  // updating config inside the actor for the 'else' case also.
  /**
    * Changes config to new value
    *
    * @param config new config as String.
    */
  def changeTo(config: String) = if (_actor != null) _actor.foreach(_ ! Change(config)) else _onChange(config)
}
