package core

import core.actors.ConfigActor._
import core.actors.ConfigActor
import akka.actor.ActorRef
import akka.pattern.ask

import scala.concurrent.Future


/**
  * Basic class-representative of config files, a compound of config hierarchy.
  * If one component is depending on other, the first one must be reloaded
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
    * DSL methods for defining dependent components for the current one. This method can be used in chains for
    * specifying dependent actors for first actor in the chain.
    *
    * @example `component1 above component11 andAbove component12` means that
    *         component11 and component12 depend on component1, or, in other words
    *         component1 is above component11 and component12 in the actor's tree
    *
    *         c1
    *        /  \
    *      c11  c12
    *
    */

  /**
   * @param dependent depending component.
   * @return depending component.
   */
  final def above(dependent: Component): Component = {
    _actor ! AddDependentActor(dependent._actor)
    this
  }

  /**
    * @param dependent depending component.
    * @return current component.
    */
  final def andAbove(dependent: Component): Component = {
    _actor ! AddDependentActor(dependent._actor)
    this
  }

  /**
    * DSL methods for defining dependencies for the current component. This method can be used in chains for
    * specifying dependencies actors for first actor in the chain.
    *
    * @example `component1 below component11 andBelow component12` means that
    *         component11 and component12 are dependencies for component1, or, in other words
    *         component1 is below component11 and component12 in the actor's tree
    *
    *      c11   c12
    *        \  /
    *         c1
    *
    */

  /**
    * @param dependency dependency component.
    * @return current component.
    */
  final def below(dependency: Component): Component = {
    dependency._actor ! AddDependentActor(_actor)
    this
  }

  /**
    * @param dependency dependency component.
    * @return current component.
    */
  final def andBelow(dependency: Component): Component = {
    dependency._actor ! AddDependentActor(_actor)
    this
  }


  /**
    * Config value getter.
    *
    * @return config value as String.
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
    * Changes config value
    *
    * @param value new config value as String.
    */
  final def changeTo(value: String) {
    _actor ! Change(value)
  }

}
