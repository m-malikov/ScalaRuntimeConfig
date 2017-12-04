package core

import core.actors.ConfigActor._
import core.actors.ConfigActor
import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
//import akka.util.Timeout

import scala.concurrent.Future
//import scala.concurrent.duration._
//import scala.collection.mutable

//object Component {
//  /** Actor system for component actors. */
//  private val _system = ActorSystem("runtimeConfig")
//
//  /** Timeout for Futures. */
//  private implicit val _timeout: Timeout = Timeout(5 seconds)
//
//  /** HashMap of application components. Stores all created Components as pairs id -> component.
//    * @see Component#_id
//    */
//  private var _components = new mutable.HashMap[Int, Component]()
//
//  /**
//    * Terminates runtime config system. Call this method for stopping the application.
//    */
//  def terminateSystem(){ _system.terminate() }
//
//  /**
//    * Getter for #_components.
//    *
//    * @return HashMap containing all created Components as pairs id -> component.
//    */
//  def components: mutable.HashMap[Int, Component] = _components
//}


/**
  * Basic class corresponding application component having some configuration, optionally dependency
  * components and dependent components. If one component is depending on other, the first one must be reloaded
  * when the second one's configuration is changed. Each component has an akka actor reliable for notifying
  * dependent components when it's being reloaded or its config is changed. Provides methods for
  * getting configuration, reloading component and changing service's settings.
  *
  * Every successor must override _getValue, _onReload, _onChange methods or exception will be thrown.
  *
  * @see Component#_getValue
  * @see Component#_onReload
  * @see Component#_onChange
  *
  * @param name name of component instance
  */
class Component protected (val name: String)(implicit system: ComponentSystem) {
  import system._

  protected def _getValue: () => String = throw new Exception("_getValue not overridden")
  protected def _onReload: () => Unit = throw new Exception("_onReload not overridden")
  protected def _onChange: String => Unit = throw new Exception("_onChange not overridden")

  // Context for futures.
  import actorSystem.dispatcher

  /** Akka actor reliable for this component */
  private var _actor: Future[ActorRef] = _

  /** Identification number of this component. Initialized with component order number. */
  final val id: Int = components.size
  components += (id -> this)


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
  final def hasDependent(dependent: Component): Component = {
    checkActor()
    if (dependent._actor == null) {
      val dependentActor = _actor.flatMap(ask(_, AddDependent(dependent._getValue, dependent._onReload, dependent._onChange)).mapTo[ActorRef])
      dependent._actor = dependentActor
    } else {
      _actor.foreach(ask(_, AddDependentActor(dependent._actor)))
    }
    this
  }


  /**
    * Config value getter.
    *
    * @return configuration of this component as String.
    */
  final def value: Future[Any] = {
    checkActor()
    _actor.flatMap(a => a ? Value)
  }

  /**
    * Reloads this component and all dependent.
    */
  final def reload() {
    checkActor()
    _actor.foreach(_ ! Reload)
  }

  /**
    * Changes config to new value
    *
    * @param value new config value as String.
    */
  final def changeTo(value: String) {
    checkActor()
    _actor.foreach(_ ! Change(value))
  }

  /**
    * Creates actor for this component if necessary
    */
  private final def checkActor() {
    if (_actor == null) _actor = Future(actorSystem.actorOf(ConfigActor.props(_getValue, _onReload, _onChange)))
  }
}
