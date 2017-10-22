package configtree

abstract class ConfigTree {
  def getHtml: String
}

object ConfigTree {
  def apply(name: String, config: Option[Any]): ConfigTree = {
    config match {
      case Some(x: Map[String, _]) =>
        val children: Iterable[ConfigTree] = for ((k: String, v) <- x) yield ConfigTree(k, Some(v))
        ConfigTreeNode(name, children)
      case x => ConfigTreeString(name, x)
    }
  }
}

case class ConfigTreeString(name: String, value: Option[Any]) extends ConfigTree {
  def getHtml: String = { value match {
    case  Some(x) => s"<em>$name:</em> $x"
    case None => s"<span style='color:red'>$name</span>"
    }
  }
}

case class ConfigTreeNode(name: String, children: Iterable[ConfigTree]) extends ConfigTree {
  def getHtml: String = {
    s"<ul><b>$name</b>" ++ children.foldLeft("") { case (z, config) =>
      z ++ "<li>" ++ config.getHtml ++ "</li>"
    } ++ "</ul>"
  }
}


