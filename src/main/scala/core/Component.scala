package core

import core.actors.ConfigActor._
import core.actors.ConfigActor
import akka.actor.ActorRef
import akka.pattern.ask

import scala.concurrent.Future


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
class Component protected (val name: String)(implicit componentSystem: ComponentSystem) {

  protected def _getValue: () => String = throw new Exception("_getValue not overridden")
  protected def _onReload: () => Unit = throw new Exception("_onReload not overridden")
  protected def _onChange: String => Unit = throw new Exception("_onChange not overridden")

  import componentSystem.actorSystem
  import componentSystem.timeout

  /** Akka actor reliable for this component */
  private var _actor: ActorRef = actorSystem.actorOf(ConfigActor.props(_getValue, _onReload, _onChange))

  /** Identification number of this component. Initialized with component order number. */
  final val id: Int = componentSystem.components.size
  componentSystem.components += (id -> this)


  /**
    * DSL method for defining dependent components for current one. This method can be used in chains for
    * specifying dependent actors for first actor in the chain.
    *
    * @example `component1 hasDependent component11 hasDependent component12` this means that
    *         component11 and component12 depend on component1.
    *
    *
    * @param dependent depending component.
    * @return current component.
    */
  final def hasDependent(dependent: Component): Component = {
    _actor ! AddDependentActor(dependent._actor)
    this
  }


  /**
    * Config value getter.
    *
    * @return configuration of this component as String.
    */
  final def value: Future[Any] = {
    _actor ? Value
  }

  /**
    * Reloads this component and all dependent.
    */
  final def reload() {
    _actor ! Reload
  }

  /**
    * Changes config to new value
    *
    * @param value new config value as String.
    */
  final def changeTo(value: String) {
    _actor ! Change(value)
  }

}
