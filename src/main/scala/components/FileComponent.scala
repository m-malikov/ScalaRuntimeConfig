package components

import core.Component

import java.nio.file._

import collection.JavaConverters._

class FileComponent(name: String, path: Path) extends Component(name) {
  override protected def _getValue {
    Files.readAllLines(path)
    .asScala
    .foldRight(new StringBuilder())((str: String, builder: StringBuilder) => builder.append("\n" + str))
    .mkString
  }

  override protected def _onReload: () => Unit = () => println(s"Reloaded File Component '$name")

  override protected def _onChange: String => Unit = (newValue: String) => Files.write(path, newValue.getBytes("utf-8"))

}
