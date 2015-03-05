package models.oauth2

import models.{User, Permission}
import play.api.db.slick.Config.driver.simple._
import scala.slick.lifted.Tag
import play.api.db.slick.DB
import play.api.Play.current

/**
 * Created by Alper on 29.01.2015.
 */
class Users(tag: Tag) extends Table[User](tag, Some("configuration") , "user") {
  def id = column[Long]("_id", O.PrimaryKey)
  def username = column[String]("username")
  def email = column[String]("email")
  def password = column[String]("password")
  def permission = column[String]("permission")

  def * = (id, username, email, password, permission) <>
    ((t : (Long,
      String, String, String, String)) => User(t._1, t._2, t._3, t._4, Permission.valueOf(t._5)),
      (u: User) => Some((u._id,u.username,u.email,u.password,u.permission.toString)))
}

object Users extends DAO {

  def findUser(username: String, password: String): Option[User] = {
    DB.withTransaction { implicit session =>
      Users.filter(u => u.username === username && u.password === password).firstOption
    }
  }

  def getById(id: Long): Option[User] = {
    DB.withTransaction { implicit session =>
      Users.filter(u => u.id === id).firstOption
    }
  }
}
