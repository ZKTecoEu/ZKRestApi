package models

import java.util.{Random, UUID}

import anorm.SqlParser._
import anorm._
import play.api.Play.current
import play.api.data.Forms._
import play.api.data._
import play.api.db._
import utils.DBHelper

import scala.collection.mutable.Seq
import scala.language.postfixOps

case class User(_id: Long, username: String, email: String, password: String , permission: Permission) {
  def asSeq(): Seq[(String, Any)] = Seq(
    "_id" -> _id,
    "username" -> username,
    "email" -> email,
    "password" -> password,
    "permission" -> permission
  )
}

case class UserForm(email:String,username:String,password:String,password2:String,permission:String,zone:String)
case class LoginForm(username:String,password:String,client_id:String,client_secret:String,grant_type:String)

object User {
  def fromParser(
                  _id: Long = 0,
                  username: String = "",
                  email: String = "unknown email",
                  password: String = "",
                  permission: String = ""
                  ): User = {
    new User (
      _id,
      username,
      email,
      password,
      Permission.valueOf(permission)
    )
  }
  // -- Parsers
  
  /**
   * Parse a User from a ResultSet
   */
  val simpleParser = {
    get[Long]("user._id") ~
    get[String]("user.username") ~
    get[String]("user.email") ~
    get[String]("user.password") ~
    get[String]("user.permission")   map {
      case _id~username~email~password~permission => User( _id, username ,email ,password , Permission.valueOf(permission))
    }
  }

  val userForm:Form[UserForm] = Form(
    mapping(
      "Email" -> email,
      "Username" -> nonEmptyText,
      "Password" -> text(minLength = 6),
      "Confirm Password" -> nonEmptyText,
      "Permission" -> nonEmptyText,
      "ZoneName" -> nonEmptyText
    )(UserForm.apply)(UserForm.unapply)verifying ("Passwords must match", f => f.password == f.password2)
  )

  val loginForm:Form[LoginForm] = Form(
    mapping(
    "username" -> nonEmptyText,
    "password" -> text(minLength = 6),
    "client_id" -> nonEmptyText,
    "client_secret" -> nonEmptyText,
    "grant_type" -> nonEmptyText
    )(LoginForm.apply)(LoginForm.unapply)
  )

//  def getUserFromCurrentLogin(user: Security.User,authToken: String): User ={
//    User(user._id,user.username,user.email, user.password, user.permission)
//  }

  // -- Queries

  /**
   * Retrieve a User from email.
   */
  def findByEmail(email: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL("select * from configuration.user where email = {email}").on(
        'email -> email
      ).as(User.simpleParser.singleOpt)
    }
  }

  /**
   *  Retrieve a User from id.
   */
  def findById(_id: Long): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL("select * from configuration.user where _id = {_id}").on(
       '_id -> _id
      ).as(User.simpleParser.singleOpt)
    }
  }
  
  /**
   * Retrieve all users.
   */
  def findAll = {
   DB.withConnection { implicit connection =>
      SQL("select * from configuration.user").as(User.simpleParser *).toList
    }
  }

  /**
   * Authenticate a User.
   */
  def authenticate(email: String, password: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
         select * from configuration.user where
         email = {email} and password = {password}
        """
      ).on(
        'email -> email,
        'password -> password
      ).as(User.simpleParser.singleOpt)
    }
  }
   
  /**
   * Create a User.
   */
  def create(user: User): User = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into user values (
            {email}, {_id}, {password}
          )
        """
      ).on(
        'email -> user.email,
        '_id -> user._id,
        'password -> user.password
      ).executeUpdate()
      
      user
      
    }
  }

  def createFromForm(user:UserForm) = {

    val _id = new Random().nextLong()

    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into configuration.user values (
             {_id}, {username} ,{email}, {password} , {permission}
          )
        """
      ).on(
          '_id -> _id,
          'username -> user.username,
          'email -> user.email,
          'password -> user.password,
          'permission -> user.permission
        ).executeUpdate()
    }

    DB.withConnection{ implicit connection =>
      SQL(
       """
         insert into configuration.user2zone values ( {id_zone} , {id_user} )
       """
      ).on(
      'id_zone -> user.zone,
      'id_user -> _id
        ).executeUpdate()

    }

  }

  def deleteUserfromDB(_id:Long) = {

    DB.withConnection{ implicit connection =>
      SQL("delete from configuration.user where _id = {_id}").on(
        '_id -> _id
      ).execute()
    }

  }

  /**
   *  Find zone Name.
   */
  def findZoneName(_id:Long) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          select z.id_zone from configuration.user u,configuration.user2zone z where z.id_user = {_id}
        """
      ).on('_id -> _id)().map(row => row[String]("id_zone")).toList.head
    }
  }

  def findAllZoneName(_id:Long) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          select id_zone from configuration.user2zone where id_user = {_id}
        """
      ).on('_id -> _id)().map(row => row[String]("id_zone")).toList
    }
  }

}
