package formatters

import anorm.{NotAssigned, Pk}
import formatters.PkFormatter._
import models.Employee
import play.api.libs.json.Json.toJson
import play.api.libs.json._

object EmployeeFormatter {

  implicit object JsonEmployeeFormatter extends Format[Employee] {

    def writes(o: Employee): JsValue = {
      toJson(Map(
        "_id" -> toJson(o._id.toString),
        "first_name" -> toJson(o.first_name),
        "last_name" -> toJson(o.last_name),
        "pin" -> toJson(o.pin),
        "birth_date" -> toJson(o.birth_date),
        "address" -> toJson(o.address),
        "gender" -> toJson(o.gender),
        "phone" -> toJson(o.phone),
        "email" -> toJson(o.email),
        "photo" -> toJson(o.photo),
        "national_id" -> toJson(o.national_id),
        "department" -> toJson(o.department),
        "enabled" -> toJson(o.enabled)
      ))
    }


    def reads(j: JsValue): JsResult[Employee] = JsSuccess({
      Employee.fromParser(
        _id = (j \ "_id").as[String],
        first_name = (j \ "first_name").as[Option[String]].getOrElse("unkown name"),
        last_name = (j \ "last_name").as[Option[String]],
        pin = (j \ "pin").as[Option[String]].getOrElse("unkonwn pin"),
        birth_date = (j \ "birth_date").as[Option[Long]],
        address = (j \ "address").as[Option[String]],
        gender = (j \ "gender").as[Option[Int]],
        phone = (j \ "phone").as[Option[String]],
        email = (j \ "email").as[Option[String]],
        photo = (j \ "photo").as[Option[String]],
        national_id = (j \ "national_id").as[Option[String]],
        department = (j \ "department").as[Option[String]],
        enabled = (j \ "enabled").as[Option[Boolean]]

      )
    })
  }

}