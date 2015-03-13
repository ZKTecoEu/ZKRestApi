package controllers.br

import javax.ws.rs.PathParam

import com.wordnik.swagger.annotations.{Api, ApiOperation, ApiParam}
import controllers.ActionBuilders.Authenticated
import formatters.EmployeeFormatter._
import formatters.LoginCombinationFormatter._
import models._
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson
import play.api.mvc._
import utils.{JsonErrorAction, JsonNotFound}

@Api(value = "/br/employee", description = "Employee Operations")
object Employees extends Controller {

  @ApiOperation(nickname = "getEmployeeById" , value = "Find employee by Id" , notes = "Returns employee according to Id",
    httpMethod = "GET" , response = classOf[models.Employee])
  def get(zone_name: String, @ApiParam(value = "Id of employee") @PathParam("id") id: Long) = Authenticated { request =>
    if(User.findAllZoneName(request.user._id).contains(zone_name)){
      Employee.findById(zone_name, id).map {
        employee => Ok(toJson(employee))
      }.getOrElse(JsonNotFound("Employee with id %s not found".format(id)))
    }
      else JsonErrorAction(request.user.username+" is not belonged to the "+zone_name)
  }

  val create = Action {
    NotImplemented
  }
  val update = Action {
    NotImplemented
  }
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
  @ApiOperation(nickname = "validateEmployee" , value = "Validate employee" , notes = "Checks employee login combination is defined or not." +
    "You can check response class section in order to find out which values need to be sent in a json object." ,
    httpMethod = "POST" , response = classOf[models.LoginCombination])
  def validate(zone_name:String) = Authenticated { request =>
    if(User.findAllZoneName(request.user._id).contains(zone_name)){
    var result:JsValue = null
      request.body.asJson.map{ json =>
        json.asOpt[LoginCombination].map {  loginCombination =>
          result = convert(Employee.checkData(loginCombination,zone_name))
      }
    }
    Ok(result)
    }
    else JsonErrorAction(request.user.username+" is not belonged to the "+zone_name)
  }

  /**
   * Check employee is null or not and render it to json object.
   *
   * @param employee
   * @return
   */
  def convert(employee: Option[Employee]): JsValue = {
    if (employee == null)
      toJson("Employee could not was validated")
    else
      toJson(employee)
  }

}