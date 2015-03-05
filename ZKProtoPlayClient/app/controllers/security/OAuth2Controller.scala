package controllers.security

import oauth2.OauthDataHandler
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scalaoauth2.provider._


/**
 * Created by alper on 25/11/14.
 */
object OAuth2Controller extends Controller with OAuth2Provider {

  def auth  = Action.async { implicit request =>
    issueAccessToken(new OauthDataHandler())
  }
}
