package controllers.br

import com.wordnik.swagger.annotations.{ApiOperation, Api}
import models.{User, AttendanceLog}
import play.api.mvc.Controller
import utils.{Authenticated, SQLHelper, DBHelper}
import formatters.AttendanceLogFormatter._

/**
 * Created by alper on 28/11/14.
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
  def insertAttendanceLog = Authenticated { request =>
      request.body.asJson.map{ json =>
        json.asOpt[AttendanceLog].map{ attendanceLog =>
          val zoneName = User.findZoneName(request.user._id)
          DBHelper.insert(SQLHelper.insertAttendanceQuery(attendanceLog,zoneName),SQLHelper.insertOpLogQuery(attendanceLog,zoneName,AttendanceLog.table),true)
        }
      }
      Ok("AttendanceLog added to database")
    }
}
