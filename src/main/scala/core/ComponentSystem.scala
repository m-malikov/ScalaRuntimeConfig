package core

import akka.actor.ActorSystem
import akka.util.Timeout

import scala.collection.mutable
import scala.concurrent.duration._

class ComponentSystem(name: String) {
    /** Actor system for component actors. */
    val actorSystem = ActorSystem(name)

    /** Timeout for Futures. */
    implicit val timeout: Timeout = Timeout(5 seconds)

    /** HashMap of application components. Stores all created Components as pairs id -> component.
      * @see Component#_id
      */
    var components = new mutable.HashMap[Int, Component]()

    /**
      * Terminates runtime config system. Call this method for stopping the application.
      */
    def terminateSystem(){ actorSystem.terminate() }
}
