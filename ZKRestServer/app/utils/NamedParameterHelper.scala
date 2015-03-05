package utils

import scala.util.matching.Regex
import scala.collection.mutable.MutableList

/**
 * Created by alper on 9/12/14.
 */
case class Query(query:String,argMap:Map[String, MutableList[Int]],count:Int)

object NamedParameterHelper {

  final val REGEX = "\\?\\w+"

  def processArguments(query:String) = {

    var argMap:Map[String, MutableList[Int]] = Map()

    val pattern = new Regex(REGEX)

    var count = 1

    for(arg <- pattern findAllIn query ){
      var positions:MutableList[Int] = MutableList()
      if(argMap.contains(arg)){
        positions = argMap(arg)
      }
        positions += count
        argMap += arg -> positions
        count = count + 1
      }

    val newQuery = query.replaceAll(REGEX,"?")

    new Query(newQuery,argMap,count-1)

  }

  def getMapOfNamedArguments(namedArgs:Map[String,Any],query:Query) = {
    var finalMap:Map[Int,Any] = Map()
    namedArgs.foreach{
      case(key, value) =>
        val list:MutableList[Int] = query.argMap(key)
        for(i <- 0  to list.length - 1) {
          finalMap += list(i) -> value
        }
    }
    finalMap
  }

  def isContainParameter(query:String) = {
    val pattern = new Regex(REGEX)
    pattern.findAllIn(query).length>0
  }

}
