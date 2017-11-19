package configtree


abstract class ConfigTree {
  def getHtml: String
  val name: String
  def update(fields: Map[String, String]): Unit
  def getTextConfig(depth: Int = 0): String
}

object ConfigTree {

  def apply(name: String, config: Option[Any]): ConfigTree = {
    config match {
      case Some(x: Map[String, _]) =>
        val children: Iterable[ConfigTree] = for ((k: String, v) <- x) yield ConfigTree(k, Some(v))
        ConfigTreeNode(name, children)
      case Some(x: List[_]) =>
        val children: List[ConfigTree] = for ((v, i) <- x.zipWithIndex) yield ConfigTree(s"${name}_$i", Some(v))
        ConfigTreeList(name, children)
      case Some(x: String) => ConfigTreeString(name, Some('"' + x + '"'))
      case x => ConfigTreeString(name, x)
    }
  }
}