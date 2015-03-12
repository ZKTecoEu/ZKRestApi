package controllers

import com.wordnik.swagger.annotations.{Api, ApiOperation}
import oauth2.OauthDataHandler
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scalaoauth2.provider.OAuth2Provider


/**
 * Created by alper on 25/11/14.
 */
@Api(value = "/oauth2",description = "Authorization Operation")
object Security extends Controller with OAuth2Provider {

  @ApiOperation(nickname = "getTokens" , value = "Validate username and password" , notes = "Checks user is defined or not ." +
    "You can send authorization request in query string,x-www-form-urlencoded or json object.You must fill these fields which are" +
    "grant_type=password , client_id={your_client_id} , client_secret={your_client_secret} , username={your_username} , " +
    "password={your_password}", httpMethod = "POST" , response = classOf[models.oauth2.AccessTokenResponse])
  def accessToken = Action.async { implicit request =>
    issueAccessToken(new OauthDataHandler())
  }

//  def login = Action { implicit request =>
//    //Ok("Make a post request in order to login!")
//    Ok(views.html.login(User.loginForm.fill(new LoginForm("","","test_client_id","test_client_secret","password"))))
//  }
//
//  def logout = Action.async { implicit request =>
//    Future.successful(Redirect(routes.Security.login).flashing(
//      "success" -> "You've been logged out"
//    ).discardingCookies(DiscardingCookie("access_token"),DiscardingCookie("refresh_token")))
//  }

}
