package controllers

/**
 * Created by Alper on 17.02.2015.
 */

import play.api.Routes
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.EssentialAction
import play.core.Router.JavascriptReverseRoute
import play.core.Router._
import routes.javascript.Security.accessToken
import routes.javascript.Application.setting
import routes.javascript.Application.clients
import routes.javascript.Application.users
import routes.javascript.Security.login
import routes.javascript.Application.createClient
import routes.javascript.Application.addUser

object JavascriptRoute extends Controller {

  /* Application related JavascriptReverse Route will goes here */
  val appRoutes: List[JavascriptReverseRoute] = List(accessToken,setting,clients,users,login,createClient,addUser)

  /* All JavascriptReverse Route will combine here */
  val javascriptRouters = appRoutes

  /**
   * This is use to generate JavascriptReverseRoute for all provided actions
   *
   * @return
   */
  def javascriptRoutes: EssentialAction = Action { implicit request =>
    import routes.javascript._
    Ok(Routes.javascriptRouter("jsRoutes")(javascriptRouters: _*)).as("text/javascript")
  }

}
