package configtree

import com.typesafe.config.{ConfigFactory, ConfigRenderOptions}
import java.io.File

import scala.util.Try
import scala.util.parsing.json.JSON





case class ConfigTreeString(name: String, var value: Option[Any]) extends ConfigTree {
  def getHtml: String = { value match {
    case  Some(x) => s"<em>$name:</em><input type='text' name='$name' value='$x'>"
    case None => s"<span style='color:red'>$name</span>"
  }}

  def update(fields: Map[String, String]): Unit = {
    value = Try(fields{name}).toOption
  }
}

case class ConfigTreeNode(name: String, children: Iterable[ConfigTree]) extends ConfigTree {
  def getHtml: String = {
    s"<ul><b>$name</b>" + children.foldLeft("") { case (z, config) =>
      z + "<li>" + config.getHtml + "</li>"
    } + "</ul>"
  }

  def update(fields: Map[String, String]): Unit = {
    children.foreach(child => child.update(fields))
  }
}

case class ConfigSupervisor(files: Seq[File]) {
  val trees: Map[String, ConfigTree] = (for (file <- files) yield {
    val conf = ConfigFactory.parseFile(file).root()
    val confJson = JSON.parseFull(conf.render(ConfigRenderOptions.concise()))
    file.getName -> ConfigTree(file.getName, confJson)
  }) toMap

  def getHtml: String = {
    trees.foldLeft("")((acc, tree) =>
      acc + s"</br><form action='/update?name=${tree._2.name}' method='post'>"
        + tree._2.getHtml + "<button type='submit'>update</button></form>")
  }

  def update(name: String, fields: Map[String, String]): Unit = {
    trees{name}.update(fields)
  }
}


