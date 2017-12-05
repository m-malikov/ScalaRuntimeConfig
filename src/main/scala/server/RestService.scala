package server

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import confightml.ConfigHtmlGenerator
import core.ComponentSystem

import scala.util.{Success, Try}

trait RestService {
  implicit val componentSystem: ComponentSystem
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val configHtmlGenerator = new ConfigHtmlGenerator()

  val route: Route = {
    get {
      pathPrefix("static") {
        getFromResourceDirectory("static")
      } ~ onComplete(configHtmlGenerator.getHtml) { (responce: Try[String]) =>
        responce match {
          case Success(html) => complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, html))
          case _ => complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "failure"))
        }
      }

    } ~ post {
      parameters('id) { (id) => {
        formFieldMap { fields =>
          configHtmlGenerator.update(id.toInt, fields{"config_value"})
          redirect("/", StatusCodes.Found)
        }
      }}
    }
  }
}
