package formatters

import models.LoginCombination
import play.api.libs.json.Json._
import play.api.libs.json.{JsSuccess, JsResult, JsValue, Format}

/**
 * Created by alper on 26/11/14.
 */
object LoginCombinationFormatter {
  implicit object JsonLoginCombinationFormatter extends Format[LoginCombination] {

    def writes(o: LoginCombination): JsValue = {
      toJson( Map(
        "pin"  	-> toJson(o.pin),
        "pwd"		-> toJson(o.pwd),
        "fp"		-> toJson(o.fp),
        "card"	-> toJson(o.card),
        "face"  -> toJson(o.face)
      ))
    }

    def reads(j: JsValue): JsResult[LoginCombination] = JsSuccess({
      LoginCombination.fromParser(
        pin         =(j \ "pin")    .as[Option[String]].getOrElse(""),
        pwd  				=(j \ "pwd")			.as[Option[String]].getOrElse(""),
        fp          =(j \ "fp") .as[Option[String]].getOrElse(""),
        card        =(j \ "card").as[Option[String]].getOrElse(""),
        face        =(j \ "face").as[Option[String]].getOrElse("")
      )
    } )
  }

}
