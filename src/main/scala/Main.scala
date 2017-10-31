import ConfigActor.{AddDependent, Update}
import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent.Future
import scala.concurrent.duration._

object Main extends App {
  // Actor stuff
  val system = ActorSystem("runtimeConfig")
  // Future stuff
  import system.dispatcher
  implicit val timeout = Timeout(5 seconds)

  /*
  *  Configs hierarchy:
  *
  *       0
  *     /   \
  *    2     1
  *       /  |  \
  *      12  11  10
  *       |      |
  *      120    100
  */

  // Every config here is a Future
  // config 0 is a Future for analogy with other configs.
  val config0 = Future(system.actorOf(ConfigActor.props(0)))
  // ask(x, AddDependent(1)).mapTo[ActorRef] returns Future[ActorRef] --- answer of actor
  // containing ActorRef to newly created config actor
  //
  // flatMap is Future method applying function to Future result when it is evaluated
  val config1 = config0.flatMap(ask(_, AddDependent(1)).mapTo[ActorRef])
  val config10 = config1.flatMap(ask(_, AddDependent(10)).mapTo[ActorRef])
  val config11 = config1.flatMap(ask(_, AddDependent(11)).mapTo[ActorRef])
  val config12 = config1.flatMap(ask(_, AddDependent(12)).mapTo[ActorRef])
  val config100 = config10.flatMap(ask(_, AddDependent(100)).mapTo[ActorRef])
  val config121 = config12.flatMap(ask(_, AddDependent(120)).mapTo[ActorRef])
  val config2 = config0.flatMap(ask(_, AddDependent(2)).mapTo[ActorRef])

  // If we use this way, we should write wrapper around AnyRef and make a DSL for convenient
  // dependency specification

  // Sending update signal to config 1. Configs 1, 10, 11, 12, 100, 120 will be updated
  config1.foreach(_ ! Update)

  system.terminate()
}
