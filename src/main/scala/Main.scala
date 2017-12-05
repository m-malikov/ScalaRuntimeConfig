import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import components.FileComponent
import core.ComponentSystem
import server.RestServer


object Main extends App {
  // TODO: Make real tests
  implicit val mainSystem: ComponentSystem = new ComponentSystem("main")
  val fileComponent1 = new FileComponent("Deathstar", Paths.get("src/main/resources", "deathstar.conf"))
  val fileComponent2 = new FileComponent("Users", Paths.get("src/main/resources", "notworking.conf"))
  val fileComponent3 = new FileComponent("Project", Paths.get("src/main/resources", "project.conf"))

  fileComponent1 above fileComponent2


  implicit val system: ActorSystem = ActorSystem("my-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  val server = new RestServer()
  server.startServer("localhost", 8080)

}
