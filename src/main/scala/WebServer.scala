import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.concurrent.Future
import configtree.ConfigWebSupervisor
import akka.http.scaladsl.server.Route

import scala.util.{Success, Try}

trait RestService {
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer

  val route: Route = {
    get {
      onComplete(ConfigWebSupervisor.getHtml) { (responce: Try[String]) =>
        responce match {
          case Success(html) => complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, html))
          case _ => complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "failure"))
        }
      }

    } ~ post {
      parameters('id) { (id) => {
        formFieldMap { fields =>
          ConfigWebSupervisor.update(id.toInt, fields{"config_value"})
          redirect("/", StatusCodes.Found)
        }
      }}
    }
  }
}

class RestServer(implicit val system:ActorSystem,
                 implicit val materializer: ActorMaterializer) extends RestService {
  def startServer(address: String, port: Int): Future[Http.ServerBinding] = {
    Http().bindAndHandle(route, address, port)
  }
}
