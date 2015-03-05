package models.oauth2

import java.util.Date
import play.api.db.slick.Config.driver.simple._
import scala.slick.lifted.Tag
import play.api.db.slick.DB
import play.api.Play.current
/**
 * Created by Alper on 29.01.2015.
 */
case class AuthCode(authorizationCode: String, userId: Long, redirectUri: Option[String], createdAt: java.sql.Timestamp, scope: Option[String], clientId: String, expiresIn: Int)
class AuthCodes(tag: Tag) extends Table[AuthCode](tag, Some("configuration") ,"auth_code") {
  def authorizationCode = column[String]("authorization_code", O.PrimaryKey)
  def userId = column[Long]("user_id")
  def redirectUri = column[Option[String]]("redirect_uri")
  def createdAt = column[java.sql.Timestamp]("created_at")
  def scope = column[Option[String]]("scope")
  def clientId = column[String]("client_id")
  def expiresIn = column[Int]("expires_in")
  def * = (authorizationCode, userId, redirectUri, createdAt, scope, clientId, expiresIn) <> ((AuthCode.apply _).tupled, AuthCode.unapply _)
}

object AuthCodes extends DAO {
  def find(code: String) = {
    DB.withTransaction { implicit session =>
      val authCode = AuthCodes.filter(a => a.authorizationCode === code).firstOption

      // filtering out expired authorization codes
      authCode.filter(p => p.createdAt.getTime + (p.expiresIn * 1000) > new Date().getTime)
    }
  }
}
