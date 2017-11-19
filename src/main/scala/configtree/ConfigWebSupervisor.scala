package configtree

import core.Component

import scala.collection.mutable
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Handles interaction between webserver and actor model
  */

object ConfigWebSupervisor {

  /**
    * Creates an string with an html form for each compoment
    */

  def getHtml: Future[String] = {
    val valuesMap = for ((id, component) <- Component.components) yield
      { ((component.id, component.name), component.getValue) }
    val valuesFuture = Future.traverse(valuesMap)
      {case (key, futureValue) => futureValue.map(key -> _)}
    val p =  Promise[String]()
    Future {
      valuesFuture.onComplete {
        case Success(list: mutable.Iterable[_]) =>
          p.success(list.foldLeft("")((acc: String, entry) =>
            acc +
              s"""${entry._1._2}</br><form action='/update?id=${entry._1._1}' method='post'>
          <textarea name="config_value">${entry._2}</textarea><br>
          <button type='submit'>update</button></form>"""))
        case Failure(ex) => p.failure(ex)
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
