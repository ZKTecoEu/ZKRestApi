package formatters

/**
 * Created by alper on 18/11/14.
 */

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.json.Json.JsValueWrapper

object DataFormatter {

  implicit object JsonDataFormatter extends Format[Map[String, Any]]{

    def writes(map: Map[String, Any]): JsValue =
      Json.obj(map.map { case (s, o) =>
        val ret: (String, JsValueWrapper) = o match {
          case _: String => s -> JsString(o.asInstanceOf[String])
          case _: Long => s -> JsNumber(o.asInstanceOf[Long])
          case _: Int => s-> JsNumber(o.asInstanceOf[Int])
          case nil => s-> JsString("")
        }
        ret
      }.toSeq: _*)


    def reads(jv: JsValue): JsResult[Map[String, Any]] =
      JsSuccess(jv.as[Map[String, JsValue]].map { case (k, v) =>
        k -> (v match {
          case s: JsString => s.as[String]
          case l: JsNumber => l.as[Long]
        })
      })
  }

}