package controllers.br

import javax.ws.rs.PathParam

import com.wordnik.swagger.annotations.{ApiParam, ApiOperation, Api}
import controllers.ActionBuilders.Authenticated
import models.User
import play.api.libs.json.JsArray
import play.api.mvc.Controller
import utils._


/**
 * Created by alper on 4/12/14.
 */
@Api(value = "/br/query" , description = "Executes generic queries")
object CustomQueries extends Controller {

  @ApiOperation(nickname = "executeQueryWithParams",value = "Execute query with parameters",
    notes = "Executes query according to parameters and responses result." +
      "Because of not being a static data model,you need to send parameters according to your query's requirements." +
      "You should send your parameters in x-www-form-urlencoded format." ,httpMethod = "POST" , response = classOf[Object])
  def queryWithParams(@ApiParam(value = "Name of query") @PathParam("query_name") query_name: String) = Authenticated { request =>
      var map:Map[String,Any] = Map()
      request.body.asFormUrlEncoded.get foreach {
        case (key,value) => value(0).toLongOpt.isInstanceOf[Some[Long]] match {
          case true => map += key -> value(0).toLong
          case false => map += key -> value(0)
        }
      }
      /*request.body.asJson.map { json =>
        map ++= DataFormatter.JsonDataFormatter.reads(json).get
      }*/
      val zone_name = User.findZoneName(request.user._id)
      val queryString = CustomQueriesGenerator.queryMap.getOrElse(query_name, "not found")
      val query = NamedParameterHelper.processArguments(queryString)
      val dataMap = NamedParameterHelper.getMapOfNamedArguments(map,query)
      val jsonList = DBHelper.executeQueryWithParams(query.query, zone_name, dataMap)
      Ok(new JsArray(scala.collection.mutable.ArraySeq(jsonList: _*)))
  }

  @ApiOperation(nickname = "executeQueryWithoutParam" , value = "Execute query without parameter" ,
    notes =  "Executes query and responses result", httpMethod = "GET" , response = classOf[Object])
  def queryWithoutParams(@ApiParam(value = "Name of query") @PathParam("query_name") query_name: String) = Authenticated { request =>
      val zone_name = User.findZoneName(request.user._id)
      val query = CustomQueriesGenerator.queryMap.getOrElse(query_name, "not found")
      val jsonList = DBHelper.executeQueryWithoutParams(query, zone_name)
      Ok(new JsArray(scala.collection.mutable.ArraySeq(jsonList: _*)))
  }

  implicit class StringImprovements(val s:String) {
    import scala.util.control.Exception._
    def toLongOpt = catching(classOf[NumberFormatException]) opt s.toLong
  }

}
