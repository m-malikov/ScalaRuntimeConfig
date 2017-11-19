import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn
import configtree.ConfigWebSupervisor
import java.io.File

import akka.http.scaladsl.server.Route
import core.Component

import scala.util.{Failure, Success, Try}

object WebServer {

  def main(args: Array[String]) {

    implicit val system: ActorSystem = ActorSystem("my-system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val component1 = new Component("main component", () => "1", () => println("update 1"), (c) => println(s"change 1 to `$c`"))

    val route: Route = {
      get {
        onComplete(ConfigWebSupervisor.getHtml) { (html: Try[String]) =>
          html match {
            case Success(html) => complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, html))
            case _ => complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "failure"))
          }
        }
        //complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, ConfigWebSupervisor.getHtml))

      } ~ post {
        parameters('id) { (id) => {
          formFieldMap { fields =>
            ConfigWebSupervisor.update(id.toInt, fields{"config_value"})
            redirect("/", StatusCodes.Found)
          }
        }}
      }
    }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}