package controllers.br

import javax.ws.rs.PathParam

import com.wordnik.swagger.annotations.{Api, ApiOperation, ApiParam}
import controllers.ActionBuilders.{Authenticated, AuthenticatedZone}
import formatters.LoginCombinationFormatter._
import formatters.EmployeeFormatter._
import models._
import play.api.Logger
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson
import play.api.mvc._
import utils.{JsonErrorAction, JsonNotFound}


@Api(value = "/br/employee", description = "Employee Operations")
object Employees extends Controller {

  @ApiOperation(nickname = "getEmployee", value = "Find employee by Id", notes = "Returns employee according to Id",
    httpMethod = "GET", response = classOf[models.Employee])
  def getEmployee(zoneName: String, @ApiParam(value = "Id of employee") @PathParam("id") id: Long) = Authenticated { request =>
    Employee.getById(zoneName, id).map {
      employee => Ok(toJson(employee))
    }.getOrElse(JsonNotFound("Employee with id %s not found".format(id)))
  }

  def get(zoneName: String) = AuthenticatedZone(zoneName) { request =>
    Ok(toJson(Employee.getEmployeeList(zoneName)))
    //    Ok(new JsArray(scala.collection.mutable.ArraySeq(Employee.getEmployeeList(zoneName):_*)))
  }

  val create = Action {
    NotImplemented
  }
  def update(zoneName: String) = AuthenticatedZone(zoneName) { request =>
    Logger.info("Entrada ")

    request.body match {
      case AnyContentAsJson(json) => Logger.info("json is "+json)
      case _ => Logger.info("body is none")
    }

    request.body.asJson.map { json =>
      Logger.info("body as json ")
      json.asOpt[Employee].map {employee =>
        Logger.info("json as opt ")
        Employee.updateEmployee(zoneName, employee)
        //TODO ZKProto
        Logger.info("Updated ")
        Ok("Updated Dentro")
      }
    }
    Ok("Updated")
  }

  def updatePhoto(zoneName: String) = AuthenticatedZone(zoneName) { request =>
    Logger.info("Entrada ")

    request.body match {
      case AnyContentAsJson(json) => Logger.info("json is "+json)
      case _ => Logger.info("body is none")
    }

    request.body.asJson.map { json =>
      Logger.info("body as json ")
      json.asOpt[Employee].map {employee =>
        Logger.info("json as opt ")
        Employee.updateEmployeePhoto(zoneName, employee)
        //TODO ZKProto
        Logger.info("Updated ")
        Ok("Updated Dentro")
      }
    }
    Ok("Updated")
  }

  /*
  * def insertAttendanceLog(zone_name:String) = Authenticated { request =>
    if(User.findAllZoneName(request.user._id).contains(zone_name)){
      request.body.asJson.map{ json =>
        json.asOpt[AttendanceLog].map{ attendanceLog =>
          //val zoneName = User.findZoneName(request.user._id)
          DBHelper.insert(SQLHelper.insertAttendanceQuery(attendanceLog,zone_name),SQLHelper.insertOpLogQuery(attendanceLog,zone_name,AttendanceLog.table),true)
        }
      }
      Ok("AttendanceLog added to database")
    }
    else JsonErrorAction(request.user.username+" is not belonged to the "+zone_name)
  }*/

  val delete = Action {
    NotImplemented
  }

  /**
   * Function takes parameters from json body in order to check this employee
   * information valid or not.
   *
   * @return
   */
  //TODO fix finding employee from pin what if only card is sent for validation?
  @ApiOperation(nickname = "validateEmployee", value = "Validate employee", notes = "Checks employee login combination is defined or not." +
    "You can check response class section in order to find out which values need to be sent in a json object.",
    httpMethod = "POST", response = classOf[models.LoginCombination])
  def validate(zone_name: String) = Authenticated { request =>
    if (User.findAllZoneName(request.user._id).contains(zone_name)) {
      var result: JsValue = null
      request.body.asJson.map { json =>
        json.asOpt[LoginCombination].map { loginCombination =>
          result = convert(Employee.checkData(zone_name, loginCombination))
        }
      }
      Ok(result)
    }
    else JsonErrorAction(request.user.username + " is not belonged to the " + zone_name)
  }

  /**
   * Check employee is null or not and render it to json object.
   *
   * @param employee
   * @return
   */
  def convert(employee: Option[Employee]): JsValue = {
    if (employee == null)
      toJson("Employee couldn't been validated")
    else
      toJson(employee)
  }

}