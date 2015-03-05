package formatters

import models.AttendanceLog
import play.api.libs.json.Json._
import play.api.libs.json.{JsSuccess, JsResult, JsValue, Format}

/**
 * Created by alper on 28/11/14.
 */
object AttendanceLogFormatter {
  implicit object JsonAttendanceLogFormatter extends Format[AttendanceLog] {

    def writes(o: AttendanceLog): JsValue = {
      toJson( Map(
        "_id"  	                -> toJson(o._id),
        "id_attendance_event"		-> toJson(o.id_attendance_event),
        "id_employee"		        -> toJson(o.id_employee),
        "login_combination"	    -> toJson(o.login_combination),
        "date"                  -> toJson(o.date),
        "status"                -> toJson(o.status),
        "annotation"            -> toJson(o.annotation),
        "processed"             -> toJson(o.processed),
        "id_device"             -> toJson(o.id_device)
      ))
    }

    def reads(j: JsValue): JsResult[AttendanceLog] = JsSuccess({
      AttendanceLog.fromParser(
        _id                         =(j \ "_id")                      .as[Option[Long]].getOrElse(0),
        id_attendance_event  				=(j \ "id_attendance_event")			.as[Option[Long]].getOrElse(0),
        id_employee                 =(j \ "id_employee")              .as[Option[Long]].getOrElse(0),
        login_combination           =(j \ "login_combination")        .as[Option[Int]].getOrElse(0),
        date                        =(j \ "date")                     .as[Option[Long]].getOrElse(0),
        status                      =(j \ "status")                   .as[Option[Int]].getOrElse(0),
        annotation                  =(j \ "annotation")               .as[Option[String]].getOrElse(""),
        processed                   =(j \ "processed")                .as[Option[Int]].getOrElse(0),
        id_device                   =(j \ "id_device")                .as[Option[String]].getOrElse("")
      )
    } )
  }
}
