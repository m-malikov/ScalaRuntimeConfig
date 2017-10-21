import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config._

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn


object WebServer {

  // Var and imperiative code caused by config using java map instead of scala
  def objectToHtml(config: ConfigObject): String = {
    var htmlString = "<ul>"
    config.forEach((k, v) => {
      htmlString = htmlString ++ "<li>"
      v match {
        case x: ConfigObject => htmlString = htmlString ++ k ++ "</li>" ++ objectToHtml(x)
        case x => htmlString = htmlString ++ k ++ " - " ++ x.render(ConfigRenderOptions.concise()) ++ "</li>"
      }
    })
    htmlString ++ "</ul>"
  }

  def main(args: Array[String]) {

    implicit val system: ActorSystem = ActorSystem("my-system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val conf: Config = ConfigFactory.load()

    val route =
      path("hello") {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, objectToHtml(conf.getObject("my.organization"))))
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