package controllers

import models.User
import oauth2.OauthDataHandler
import play.api
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scalaoauth2.provider.OAuth2Provider


/**
 * Controller class for all the ActionBuilders
 *
 * @author william.merino@zktechnology.eu
 */
object ActionBuilders extends Controller with OAuth2Provider {


  /**
   *
   * @author william.merino@zktechnology.eu
   */
  case class AuthenticatedRequest[A](user: User, request: Request[A]) extends WrappedRequest(request)



  /**
   * Action Builder for Authenticated Requests, it returns the current user.
   */
  object Authenticated extends api.mvc.ActionBuilder[AuthenticatedRequest]  {

    def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]) = {
      authenticate(block)(request)
    }
  }


  /**
   * Performs an authentication and checks if the input zone is valid.
   * @param block
   * @param zoneName
   * @param request
   * @tparam A
   * @return
   */
  private def authenticateAndCheckZone[A]( block: AuthenticatedRequest[A] => Future[Result],zoneName:String)(implicit request: Request[A]) = {
    authorize(new OauthDataHandler()) { authInfo =>
      if(User.findAllZoneName(authInfo.user._id).contains(zoneName))
        block(AuthenticatedRequest(authInfo.user, request))
      else
        Future.successful(Unauthorized)
    }
  }

  /**
   * Performs an authentication
   * @param block
   * @param request
   * @tparam A
   * @return
   */
  private def authenticate[A](block: AuthenticatedRequest[A] => Future[Result])(implicit request: Request[A]) = {
    authorize(new OauthDataHandler()) { authInfo =>
      block(AuthenticatedRequest(authInfo.user, request))
    }
  }

  /**
   * Action Builder for Authenticated requests and also checking if the user belongs to the zone
   * @param zoneName
   * @return
   */
  def AuthenticatedZone(zoneName:String) = new ActionBuilder[AuthenticatedRequest] {
    def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]) = {
      authenticateAndCheckZone(block,zoneName)(request)
    }


  }


}

