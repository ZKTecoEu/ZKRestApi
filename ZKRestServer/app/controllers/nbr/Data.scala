package controllers.nbr

import javax.ws.rs.PathParam

import com.wordnik.swagger.annotations.{ApiParam, ApiOperation, Api}
import controllers.ActionBuilders.Authenticated
import models.User
import play.api.libs.json.JsArray
import play.api.mvc._
import utils.{Authenticated,DBHelper}


@Api(value = "/data",description = "Represents raw data")
object Data extends Controller{

  /**
   * Show single row according to table_name and id.
   *
   * @param table_name
   * @param id
   * @return
   */
  @ApiOperation(nickname = "getDataById" , value = "Find data by Id", notes = "Returns data according to Id",httpMethod = "GET",response = classOf[Object])
  def show(@ApiParam(value = "Name of database table") @PathParam("table_name") table_name: String,
           @ApiParam(value = "Id of database table") @PathParam("id") id: Long) = Authenticated { request =>
      val zone_name = User.findZoneName(request.user._id)
      val jsonList = DBHelper.showData(table_name,zone_name,id)
    Ok(new JsArray(scala.collection.mutable.ArraySeq(jsonList:_*)))
    }
    else JsonErrorAction(request.user.username+" is not belonged to the "+zone_name)
  }

  /**
   * Show all related data according to table_name.
   *
   * @param table_name
   * @return
   */
  @ApiOperation(nickname = "getAllData", value = "Find all related data", notes = "Returns all related data", httpMethod = "GET",response = classOf[Object])
  def showAll(@ApiParam(value = "Name of database table") @PathParam("table_name") table_name:String) = Authenticated { request =>
      val zone_name = User.findZoneName(request.user._id)
      val jsonList = DBHelper.showAllData(table_name,zone_name)
      Ok(new JsArray(scala.collection.mutable.ArraySeq(jsonList:_*)))
    }
    else JsonErrorAction(request.user.username+" is not belonged to the "+zone_name)
  }

}