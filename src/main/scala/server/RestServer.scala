package server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import core.ComponentSystem

import scala.concurrent.Future



class RestServer(implicit val componentSystem: ComponentSystem,
                 implicit val system: ActorSystem,
                 implicit val materializer: ActorMaterializer) extends RestService {
  def startServer(address: String, port: Int): Future[Http.ServerBinding] = {
    Http().bindAndHandle(route, address, port)
  }
}
