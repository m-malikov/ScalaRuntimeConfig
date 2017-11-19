package components

import core.Component

import java.nio.file._

import collection.JavaConverters._

class FileComponent(name: String, path: Path) extends Component(
  name,
  () => {
    Files.readAllLines(path)
    .asScala
    .foldRight(new StringBuilder())((str: String, builder: StringBuilder) => builder.append("\n" + str))
    .mkString
  },
  () => {
    println("Reloaded File Component")
  },
  (newValue: String) => {
    Files.write(path, newValue.getBytes("utf-8"))
  }) {}
