import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn
import configtree.ConfigSupervisor

import java.io.File

object WebServer {

  def main(args: Array[String]) {

    implicit val system: ActorSystem = ActorSystem("my-system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val configSupervisor = ConfigSupervisor(Seq(new File("src/main/resources/deathstar.conf"),
      new File("src/main/resources/notworking.conf"),
      new File("src/main/resources/project.conf")))

    val route = {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, configSupervisor.getHtml))
      } ~ post {
        parameters('name) { (name) => {
          formFieldMap { fields =>
            configSupervisor.update("deathstar.conf", fields)
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