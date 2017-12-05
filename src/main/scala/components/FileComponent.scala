package components

import java.nio.file._

import core.{Component, ComponentSystem}

import scala.collection.JavaConverters._

class FileComponent(name: String, path: Path)(implicit componentSystem: ComponentSystem)
  extends Component(name) {
  override protected def _getValue: () => String = () =>
    Files.readAllLines(path)
      .asScala
      .foldRight(new StringBuilder())((str: String, builder: StringBuilder) => builder.append("\n" + str.reverse))
      .reverse
      .mkString

  override protected def _onReload: () => Unit = () => println(s"Reloaded File Component '$name'")

  override protected def _onChange: String => Unit = (newValue: String) => Files.write(path, newValue.getBytes("utf-8"))

}
