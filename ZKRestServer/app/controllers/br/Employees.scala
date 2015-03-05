package controllers.br

import javax.ws.rs.PathParam

import com.wordnik.swagger.annotations.{ApiParam, ApiOperation, Api}
import formatters.EmployeeFormatter._
import models._
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson
import play.api.mvc._
import utils.{Authenticated,JsonNotFound}
import formatters.LoginCombinationFormatter._

@Api(value = "/br/employee" , description = "Employee Operations")
object Employees extends Controller {

  @ApiOperation(nickname = "getEmployeeById" , value = "Find employee by Id" , notes = "Returns employee according to Id",
    httpMethod = "GET" , response = classOf[models.Employee])
  def details(@ApiParam(value = "Id of employee") @PathParam("id") id: Long) = Authenticated { request =>
      Employee.findById(id, User.findZoneName(request.user._id)).map {
        employee => Ok(toJson(employee))
      }.getOrElse(JsonNotFound("Employee with id %s not found".format(id)))
  }

  val create = Action {NotImplemented}
  val update = Action {NotImplemented}
  val delete = Action {NotImplemented}

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
  def validate = Authenticated { request =>
      var result:JsValue = null
      request.body.asJson.map{ json =>
        json.asOpt[LoginCombination].map {  loginCombination =>
          result = convert(Employee.checkData(loginCombination,User.findZoneName(request.user._id)))
      }
    }
    Ok(result)
  }

  /**
   * Check employee is null or not and render it to json object.
   *
   * @param employee
   * @return
   */
  def convert(employee:Option[Employee]):JsValue = {
    if(employee==null)
      toJson("Employee could not was validated")
    else
      toJson(employee)
  }

}