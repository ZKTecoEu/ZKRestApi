package formatters

import play.api.libs.json.Json.toJson
import play.api.libs.json.{JsSuccess, JsResult, JsValue, Format}

import anorm.Pk
import anorm.NotAssigned
import anorm.Id

object PkFormatter {

  implicit object JsonPkFormatter extends Format[Pk[Long]] {

    def writes(pk: Pk[Long]): JsValue = {
      toJson(
        pk.map(id=>id).getOrElse(0L)
      )
    }

    def reads(j: JsValue): JsResult[Pk[Long]] = JsSuccess( {
      j.as[Long] match {
        case 0 => NotAssigned
        case id => Id[Long](id)
      }
    })

  }
  }
