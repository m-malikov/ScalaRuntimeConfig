package configtree

import com.typesafe.config.{ConfigFactory, ConfigRenderOptions}
import java.io.File

import scala.util.Try
import scala.util.parsing.json.JSON

case class ConfigTreeString(name: String, var value: Option[Any]) extends ConfigTree {
  def getHtml: String = { value match {
    case  Some(x) => s"<input type='text' name='$name' value='$x'>"
    case None => s"<span style='color:red'>error</span>"
  }}

  def update(fields: Map[String, String]): Unit = {
    value = Try(fields{name}).toOption
  }

  def getTextConfig(depth : Int = 0): String = { value match {
    case Some(x) => x.toString
    case None => "???"
  }}
}

case class ConfigTreeNode(name: String, children: Iterable[ConfigTree]) extends ConfigTree {
  def getHtml: String = {
    "<ul>" + children.foldLeft("") { case (z, config) =>
      z + "<li>" + s"<b>${config.name}:</b>" + config.getHtml + "</li>"
    } + "</ul>"
  }

  def update(fields: Map[String, String]): Unit = {
    children.foreach(child => child.update(fields))
  }

  def getTextConfig(depth : Int = 0): String = {
    children.foldLeft("") {(z, config: ConfigTree) =>
      "  " * (depth+1) + config.name + " = " + config.getTextConfig(depth + 1) + "\n"
    }
  }
}

case class ConfigTreeList(name: String, children: List[ConfigTree]) extends ConfigTree {
   def getHtml: String = {
    s"<ul>" + children.foldLeft("") { case (z, config) =>
      z + "<li>" + config.getHtml + "<button>X</button></li>"
    } + "</ul><button>add</button>"
  }

  def update(fields: Map[String, String]): Unit = {
    children.foreach(child => child.update(fields))
  }

  def getTextConfig(depth : Int = 0): String = {
    "[\n" + children.foldLeft("") {(z, config) =>
      "  " * (depth+1) + config.getTextConfig(depth + 1)
    } + "  " * depth + "]\n"
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
      acc + s"${tree._1}</br><form action='/update?name=${tree._2.name}' method='post'>"
        + tree._2.getHtml + "<button type='submit'>update</button></form>")
  }

  def update(name: String, fields: Map[String, String]): Unit = {
    trees{name}.update(fields)
    println(trees{name}.getTextConfig())
  }
}


