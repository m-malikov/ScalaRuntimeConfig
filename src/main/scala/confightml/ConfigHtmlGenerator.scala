package confightml

import java.nio.file.{Files, Paths}

import core.Component

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

/**
  * Handles interaction between webserver and actor model
  */

object ConfigHtmlGenerator {

  /**
    * Generates a string with an html form for a config
    * @param name a string that will be shown as a form header
    * @param id an id of the form to put in a POST request
    * @param value config initial value
    * @return a string with an html form
    */

  private def createForm(name: String, id: String, value: String) =
    s"""|$name
        |  <div class=config>
        |   <div class="editor" id="editor-$id">$value</div>
        |   <br>
        |   <form action='update?id=$id' method='POST'>
        |     <input type="hidden" name="config_value" id="input-$id"/>
        |     <button id="button-$id">update</button>
        |   </form>
        |   <br>
        |  </div>
        |""".stripMargin

  /**
    * Iterates over all configs to create an HTML representation of all configs
    * @return a future with an HTML string containing a form for every config
    */

  private def configsToHtml: Future[String] = {
    val valuesMap = for ((_, component) <- Component.components) yield
      { ((component.id, component.name), component.value) }
    val valuesFuture = Future.traverse(valuesMap)
      {case (key, futureValue) => futureValue.map(key -> _)}
    val p =  Promise[String]()
    Future {
      valuesFuture.onComplete {
        case Success(list: mutable.Iterable[_]) =>
          p.success(list.foldLeft("")((acc: String, entry) =>
            acc + createForm(entry._1._1.toString, entry._1._1.toString, entry._1._2)))
        case Failure(ex) => p.failure(ex)
      }
    }
    p.future
  }


  /**
    * A complete HTML page for updating configs
    * @return a future of string with an HTML code
    */
  def getHtml: Future[String] = {
    val p = Promise[String]()
    Future {
      configsToHtml.onComplete {
        case Success(configsHtml) =>
          val template = Files.readAllLines(Paths.get("src/main/resources/static", "index.html"))
            .asScala
            .foldRight(new StringBuilder())((str: String, builder: StringBuilder) => builder.append("\n" + str.reverse))
            .reverse
            .mkString
          p.success(template.replace("{% configs %}", configsHtml))
        case Failure(ex) =>
          p.failure(ex)
      }
    }
    p.future
  }

  /**
    * Updates a compoment
    * @param id id of a component to be changed
    * @param value new value for a component
    */

  def update(id: Int, value: String): Unit = {
    Component.components{id}.changeTo(value)
  }
}
