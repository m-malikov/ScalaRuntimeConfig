package configtree

abstract class ConfigTree {
  def getHtml: String
  val name: String
  def update(fields: Map[String, String]): Unit
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