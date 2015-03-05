package formatters

import play.api.libs.json.{JsSuccess, JsResult, Format, JsValue}
import play.api.libs.json.Json.toJson

import models.Error

object ErrorFormatter {

  implicit object JsonErrorFormatter extends Format[Error] {

    def writes(o: Error): JsValue = {
      toJson( Map(
        "status"            -> toJson(o.status),
        "errorCode"         -> toJson(o.errorCode),
        "field"             -> toJson(o.field),
        "message"           -> toJson(o.message),
        "developerMessage"  -> toJson(o.developerMessage)
      ))
    }

    def reads(j: JsValue): JsResult[Error] = JsSuccess({
      new Error(
        status            = (j \ "status").as[Int],
        errorCode         = (j \ "errorCode").as[Int],
        field             = (j \ "field").as[String],
        message           = (j \ "message").as[String],
        developerMessage  = (j \ "developerMessage").as[String]
      )
    })

  }

}