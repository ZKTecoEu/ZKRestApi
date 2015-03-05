package utils

import scala.xml.XML

/**
 * Created by Alper on 03.02.2015.
 */
object CustomQueriesGenerator {

  var queryMap: Map[String, String] = Map()

  /**
   * This function parses generic_queries.xml file and generate map from
   * queries' name and statement.
   */
  def generateQueryMap = {
    val xml = XML.loadFile("generic_queries.xml")
    val queries = xml \ "query"
    queryMap = queries map {
      x => ((x \ "@name").text -> (x \ "statement").text)
    } toMap
  }
}
