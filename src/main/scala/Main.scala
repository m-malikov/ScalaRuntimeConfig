import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import components.TestComponent
import server.RestServer

//import scala.concurrent.Await
//import scala.concurrent.duration._


object Main extends App {
//  var comp1 = new FileComponent(Paths.get("src/main/resources/abc"))
//  println(Await.result(comp1.getValue, Duration.Inf))
//  comp1.reload()
//  comp1.changeTo("akka kakka")
  /*
  *  Configs hierarchy:
  *
  *          1       2
  *       /     \  /
  *      12      11
  *    /  |      | \
  *  122 121    111 112
  *                 |
  *                 1121
  */
  // TODO: Make real tests

  val component1 = new TestComponent("1", "1")
  val component11 = new TestComponent("11", "11")
  val component12 = new TestComponent("12", "12")
  val component111 = new TestComponent("111", "111")
  val component112 = new TestComponent("112", "112")
  val component1121 = new TestComponent("1121", "1121")
  val component121 = new TestComponent("121", "121")
  val component122 = new TestComponent("122", "122")
  val component2 = new TestComponent("2", "2")

  // Component is initialised when Component#hasDependent is called or when it is passed to this function as a param.
  component1 hasDependent component11 hasDependent component12
  component11 hasDependent component111 hasDependent component112
  component12 hasDependent component121 hasDependent component122
  component112 hasDependent component1121
  component2 hasDependent component11
  Thread.sleep(2000)

  // TODO: Find better context
  // import scala.concurrent.ExecutionContext.Implicits.global

  implicit val system: ActorSystem = ActorSystem("my-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  val server = new RestServer()
  server.startServer("localhost", 8080)

}
