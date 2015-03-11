package controllers

import java.util.Random

import formatters.DataFormatter.JsonDataFormatter
import models.{Client, ClientForm, User}
import play.api.libs.json.{JsValue, JsArray, Json}
import play.api.libs.json.Json.toJson
import play.api.mvc._
import utils.Authenticated

object Application extends Controller {

  //  def index = AuthAction(NormalUser) { implicit request =>
  //      println("authenticated")
  //      Future(Ok(views.html.index("Hi")))
  //  }

//  def index = Action.async { implicit request =>
//    authorize(new OauthDataHandler()) { authInfo =>
//      val user = authInfo.user // User is defined on your system
//      // access resource for the user
//      Future(Ok("User:"+user.email))
//    }
//  }


  def index = Authenticated { request =>
    Ok(Json.obj("userName" -> request.user.email))
  }

  def zones = Authenticated { request =>
    val jsonList = scala.collection.mutable.MutableList[JsValue]()
    User.findAllZoneName(request.user._id) foreach {
      zoneName => jsonList += JsonDataFormatter.writes(Map("zone_name"->zoneName))
    }
    Ok(new JsArray(scala.collection.mutable.ArraySeq(jsonList:_*)))
  }

  def options(url: String) = Action {
    Ok("").withHeaders("Access-Control-Allow-Origin" -> "*",
      "Allow" -> "*",
      "Access-Control-Allow-Methods" -> "POST, GET, PUT, DELETE, OPTIONS",
      "Access-Control-Allow-Headers" -> "Origin, X-Requested-With, Content-Type, Accept, Referer, User-Agent");
  }

  def name(name: String) = Action {
    Ok(toJson("Hello," + name + " from Restful Service"))
  }

  def setting = Authenticated { implicit request =>
    Ok(views.html.settings.render())
  }

  def createClient = Authenticated { implicit request =>
    Ok(views.html.createClient(Client.clientForm.fill(ClientForm(new Random().nextLong(),"","",""))))
  }

  def addUser = Authenticated { implicit request =>
    Ok(views.html.createUser(User.userForm))
  }

  def addUsertoDB = Authenticated { implicit request =>
    val token = request.cookies.get("access_token").get.value
    User.userForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.createUser(formWithErrors)),
      user => {
        User.createFromForm(user)
        Redirect("/users?access_token="+token).flashing("success"->"User created successfully!")
      }
    )
  }

  def removeUser(id:Long) = Authenticated { implicit request =>
    User.deleteUserfromDB(id)
    val token = request.cookies.get("access_token").get.value
    Redirect("/users?access_token="+token).flashing("success"->"User deleted successfully!")
  }

  def addClientoDB = Authenticated { implicit request =>
    val token = request.cookies.get("access_token").get.value
    Client.clientForm.bindFromRequest.fold(
      formwitherrors => BadRequest(views.html.createClient(formwitherrors)),
      client => {
        Client.createFromForm(client)
        Redirect("/clients?access_token="+token).flashing("success"->"Client created successfully!")
      }
    )
  }

  def users = Authenticated { implicit request =>
    Ok(views.html.users(User.findAll))
  }

  def clients = Authenticated{ implicit request =>
    Ok(views.html.clients(Client.findAll))
  }

  def removeClient(id:Long) = Authenticated { implicit request =>
    Client.deleteUserfromDB(id)
    val token = request.cookies.get("access_token").get.value
    Redirect("/clients?access_token="+token).flashing("success" -> "Client deleted successfully!")
  }

  def swagger = Action {
    request => Ok(views.html.swagger())
  }



}