package controllers

import models.User
import oauth2.OauthDataHandler
import play.api
import play.api.Logger
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scalaoauth2.provider.OAuth2Provider


//object AuthActionBuilder extends ActionBuilder[Request] {
//  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
//    Logger.info("Calling action")
//    block(request)
//  }
//}


object ActionBuilders extends Controller with OAuth2Provider {


  /**
   *
   * @author william.merino@zktechnology.eu
   */
  case class AuthenticatedRequest[A](user: User, request: Request[A]) extends WrappedRequest(request)

  def authenticate[A](block: AuthenticatedRequest[A] => Future[Result])(implicit request: Request[A]) = {
    authorize(new OauthDataHandler()) { authInfo =>
      block(AuthenticatedRequest(authInfo.user, request))
    }
  }

  object Authenticated extends api.mvc.ActionBuilder[AuthenticatedRequest]  {

    def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]) = {
      authenticate(block)(request)
    }
  }

  case class Logging[A](action: Action[A]) extends Action[A] {
    def apply(request: Request[A]): Future[SimpleResult] = {
      Logger.info("Calling action")
      action(request)
    }
    lazy val parser = action.parser
  }


}

