import core.Component

object Main extends App {
  /*
  *  Configs hierarchy:
  *
  *          1           2
  *       /     \
  *      12     11
  *    /  |      | \
  *  122 121    111 112
  *                 |
  *                 1121
  */

  val component1 = new Component(() => "1", () => println("update 1"), (c) => println(s"change 1 to `$c`"))
  val component11 = new Component(() => "11", () => println("update 11"), (c) => println(s"change 11 to `$c`"))
  val component12 = new Component(() => "12", () => println("update 12"), (c) =>  println(s"change 12 to `$c`"))
  val component111 = new Component(() => "111", () => println("update 111"), (c) => println(s"change 111 to `$c`"))
  val component112 = new Component(() => "112", () => println("update 112"), (c) => println(s"change 112 to `$c`"))
  val component1121 = new Component(() => "1121", () => println("update 1121"), (c) => println(s"change 1121 to `$c`"))
  val component121 = new Component(() => "121", () => println("update 121"), (c) => println(s"change 121 to `$c`"))
  val component122 = new Component(() => "122", () => println("update 122"), (c) => println(s"change 122 to `$c`"))

  // This one won't be initialised, because Component#hasDependent is not called
  val component2 = new Component(() => "2", () => println("update 2"), (c) => println(s"change 2 to `$c`"))

  // Component is initialised when Component#hasDependent is called or when it is passed to this function as a param.
  component1 hasDependent component11 hasDependent component12
  component11 hasDependent component111 hasDependent component112
  component12 hasDependent component121 hasDependent component122
  component112 hasDependent component1121

  println(component112.getConfig())

  // This would not work:
  // component11 hasDependent component111 hasDependent component112
  // component1 hasDependent component11 hasDependent component12

  Thread.sleep(2000)

  component11 changeTo "1"

  // List of components
//  Component.getComponents.keySet.foreach(println(_))

  Component.terminateSystem()
}
