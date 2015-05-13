package controllers

import filters.CorsFilter
import play.api._
import play.api.mvc.{RequestHeader, EssentialAction}
import utils.CustomQueriesGenerator

object Global extends GlobalSettings {

  //override def onError(request: RequestHeader, ex: Throwable) = {
  //   Future.successful(JsonErrorInternal("Exception caught",ex.toString()))
  //  }
  //
  //override def onHandlerNotFound(request:RequestHeader) = {
  //   Future.successful(JsonErrorAction("Action not found","Requested action not defined"))
  //}
  //
  //override def onBadRequest(request: RequestHeader, error: String) = {
  //   Future.successful(JsonErrorAction("Bad request",error))
  //}

  /**
   * When application starts generic queries map is created.
   *
   * @param app
   */
  override def onStart(app: Application) = {
    CustomQueriesGenerator.generateQueryMap
  }

  override def doFilter(action: EssentialAction) =   CorsFilter (action)

}