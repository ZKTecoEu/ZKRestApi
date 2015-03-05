package utils


import models.User
import oauth2.OauthDataHandler
import play.api.mvc._

import scala.concurrent.Future
import scalaoauth2.provider.OAuth2Provider

import scala.concurrent.ExecutionContext.Implicits.global


//object AuthActionBuilder extends ActionBuilder[Request] {
//  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
//    Logger.info("Calling action")
//    block(request)
//  }
//}


/**
 *
 * @author william.merino@zktechnology.eu
 */
case class AuthenticatedRequest[A](user: User, request: Request[A]) extends WrappedRequest(request)

object Authenticated extends ActionBuilder[AuthenticatedRequest] with OAuth2Provider {

  def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]) = {
    authenticate(block)(request)
  }

  def authenticate[A]( block: AuthenticatedRequest[A] => Future[Result])(implicit request: Request[A]) = {
    authorize(new OauthDataHandler()) { authInfo =>
      block(AuthenticatedRequest(authInfo.user, request))
    }
  }
}

