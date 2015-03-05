package formatters

import models.User
import play.api.libs.json.Json._
import play.api.libs.json.{JsSuccess, JsResult, JsValue, Format}

/**
 * Created by alper on 25/11/14.
 */
object UserFormatter {

  implicit object JsonUserFormatter extends Format[User] {

    def writes(o: User): JsValue = {
      toJson( Map(
        "_id"  			-> toJson(o._id),
        "username"  -> toJson(o.username),
        "email"		-> toJson(o.email),
        "permission"				-> toJson(o.permission.toString)
      ))
    }

    def reads(j: JsValue): JsResult[User] = JsSuccess({
      User.fromParser(
        _id 				=(j \ "_id")			.as[Long],
        username    =(j \ "username") .as[Option[String]].getOrElse(""),
        email       =(j \ "email")    .as[Option[String]].getOrElse(""),
        password    =(j \ "password") .as[Option[String]].getOrElse(""),
        permission  =(j \ "permission").as[Option[String]].getOrElse("")
      )
    } )
  }
}
