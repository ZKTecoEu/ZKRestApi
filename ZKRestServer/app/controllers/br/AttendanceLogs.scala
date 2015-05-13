package controllers.br

import com.wordnik.swagger.annotations.{ApiOperation, Api}
import controllers.ActionBuilders.AuthenticatedZone
import models.AttendanceLog
import play.api.mvc.Controller
import utils.{ SQLHelper, DBHelper}
import formatters.AttendanceLogFormatter._

/**
 * @author alper
 */
@Api(value = "/br/attendancelog" , description = "Attendance Log Operations")
object AttendanceLogs extends Controller {

  /**
   * Function takes parameters from json body and insert them to database.
   *
   * @return
   */
  @ApiOperation(nickname = "insertAttendanceLog" , value = "Insert attendance log" ,
    notes = "Adds attendance log to the Database." +
    "You can check response class section in order to find out which values need to be sent in a json object." ,
    httpMethod = "POST" , response = classOf[models.AttendanceLog])
  def insertAttendanceLog(zone_name:String) = AuthenticatedZone(zone_name) { request =>
      request.body.asJson.map{ json =>
        json.asOpt[AttendanceLog].map{ attendanceLog =>
          DBHelper.insert(SQLHelper.insertAttendanceQuery(attendanceLog,zone_name),SQLHelper.insertOpLogQuery(attendanceLog,zone_name,AttendanceLog.table),true)
        }
      }
      Ok("AttendanceLog added to database")
  }
}
