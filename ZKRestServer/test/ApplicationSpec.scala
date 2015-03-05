

import controllers.br.CustomQueries
import formatters.AttendanceLogFormatter
import models.AttendanceLog
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.{GlobalSettings, Application, Logger}
import play.api.http.HeaderNames
import play.api.libs.json.{JsString, Json}

import play.api.test._
import play.api.test.Helpers._
import formatters.UserFormatter.JsonUserFormatter
import utils.{CustomQueriesGenerator, NamedParameterHelper}

import scala.concurrent.Future

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends PlaySpecification {

  var access_token = "Bearer "

  "Application" should {

    "Authorization" in new WithApplication {
      val result = route(FakeRequest(POST, "/oauth2/access_token").withFormUrlEncodedBody(("grant_type", "password"),
        ("client_id", "test_client_id"), ("client_secret", "test_client_secret"), ("username", "test_user"), ("password", "test_password"))).get
      access_token += (contentAsJson(result) \ "access_token").as[String]
      assert((contentAsJson(result) \ "token_type").as[String] == "Bearer")
    }

    "User validation" in new WithApplication {
      val json = Json.obj(
        "pin" -> JsString("111111111"),
        "pwd" -> JsString("admin"),
        "fp" -> JsString(""),
        "card" -> JsString(""),
        "face" -> JsString("")
      )

      val req = FakeRequest(POST,"/br/employee/validate").withBody(json).
        withHeaders(HeaderNames.AUTHORIZATION->access_token,HeaderNames.CONTENT_TYPE -> "application/json")

      val result = route(req).get

      status(result) mustEqual OK
      contentType(result) must beSome("application/json")
      (contentAsJson(result) \ "first_name").as[String] must_== "Administrator"
    }

    "User details" in new WithApplication {
        val req = FakeRequest(GET,"/br/employee/1").
          withHeaders(HeaderNames.AUTHORIZATION->access_token)

        val result = route(req).get

        status(result) mustEqual OK
        contentType(result) must beSome("application/json")
        (contentAsJson(result) \ "first_name").as[String] must_== "Administrator"
    }

    //TODO:After test delete added data!
    "Attendance Log insertion" in new WithApplication{
        val req = FakeRequest(POST,"/br/attendancelog").withBody(AttendanceLogFormatter.JsonAttendanceLogFormatter.writes(
          new AttendanceLog(123456789,1,1,3,123123,1,"Work Time",0,"Alper's ZPAD"))).
          withHeaders(HeaderNames.AUTHORIZATION->access_token)

        val result = route(req).get

        status(result) mustEqual OK
        contentAsString(result) must_== "AttendanceLog added to database"
    }

    //TODO:Fix broken queries!
    /*
    "Generic queries without parameters" in new WithApplication {

      CustomQueriesGenerator.queryMap.foreach {
        case (k,v) =>
          if(!NamedParameterHelper.isContainParameter(v)){

            Logger.info("Query:"+k)

            val result = route(FakeRequest(GET,"/br/query/"+k).
              withHeaders(HeaderNames.AUTHORIZATION->access_token)).get

            contentType(result) must beSome("application/json")
            contentAsString(result) must not contain("errorCode")
          }
      }

    } */

  }


}
