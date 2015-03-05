package formatters

import play.api.libs.json._
import play.api.libs.json.Json.toJson
import models.Employee
import PkFormatter._
import anorm.Pk
import anorm.NotAssigned

object EmployeeFormatter {

  implicit object JsonEmployeeFormatter extends Format[Employee] {
    
    def writes(o: Employee): JsValue = {
      toJson( Map(
      "_id"  			-> toJson(o._id),
      "first_name"		-> toJson(o.first_name),
      "last_name"		-> toJson(o.last_name),
      "pin"				-> toJson(o.pin),
      "birth_date"		-> toJson(o.birth_date),
      "address"			-> toJson(o.address),
      "gender"			-> toJson(o.gender),
      "phone"			-> toJson(o.phone),
      "email"			-> toJson(o.email),
      "photo"			-> toJson(o.photo),
      "national_id"		-> toJson(o.national_id)
      ))
    }

    /*implicit val employeeRead = (
      (__ \ "_id").read[Pk[Long]] and
      (__ \ "first_name").read[String] and
      (__ \ "last_name").read[String] and
      (__ \ "pin").read[String] and
      (__ \ "birth_date").read[Long] and
      (__ \ "address").read[String] and
      (__ \ "gender").read[Int] and
      (__ \ "phone").read[String] and
      (__ \ "email").read[String] and
      (__ \ "photo").read[String] and
      (__ \ "national_id").read[String]
      )(Employee)*/

    def reads(j: JsValue): JsResult[Employee] = JsSuccess({
      Employee.fromParser(
          _id 				=(j \ "_id")			.as[Option[Pk[Long]]].getOrElse(NotAssigned),
          first_name		=(j \ "first_name")		.as[Option[String]].getOrElse("unkown name"),
          last_name         =(j \ "last_name")		.as[Option[String]],
          pin				=(j \ "pin")			.as[Option[String]].getOrElse("unkonwn pin"),
          birth_date		=(j \ "birth_date")		.as[Option[Long]],
          address			=(j \ "address")		.as[Option[String]],
          gender			=(j \ "gender")			.as[Option[Int]],
          phone				=(j \ "phone")			.as[Option[String]],
          email				=(j \ "email")			.as[Option[String]],
          photo				=(j \ "photo")			.as[Option[String]],
          national_id		=(j \ "national_id")	.as[Option[String]]
          )
    } )
  }
}