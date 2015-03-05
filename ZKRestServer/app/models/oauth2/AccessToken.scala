package models.oauth2


import play.api.db.slick.Config.driver.simple._
import scala.slick.lifted.Tag
import play.api.db.slick.DB
import play.api.Play.current

/**
 * Created by Alper on 29.01.2015.
 */
case class AccessTokenResponse(token_type:String,access_token:String,expires_in:Int,refresh_token:String)
case class AccessToken(accessToken: String, refreshToken: Option[String], userId: Long, scope: Option[String], expiresIn: Int, createdAt: java.sql.Timestamp, clientId: String)
class AccessTokens(tag: Tag) extends Table[AccessToken](tag, Some("configuration") , "access_token") {
  def accessToken = column[String]("access_token", O.PrimaryKey)
  def refreshToken = column[Option[String]]("refresh_token")
  def userId = column[Long]("user_id")
  def scope = column[Option[String]]("scope")
  def expiresIn = column[Int]("expires_in")
  def createdAt = column[java.sql.Timestamp]("created_at")
  def clientId = column[String]("client_id")
  def * = (accessToken, refreshToken, userId, scope, expiresIn, createdAt, clientId) <> ((AccessToken.apply _).tupled, AccessToken.unapply _)
}

object AccessTokens extends DAO {
  def create(accessToken: AccessToken) = {
    DB.withTransaction { implicit session =>
      AccessTokens.insert(accessToken)
    }
  }

  def deleteExistingAndCreate(accessToken: AccessToken, userId: Long, clientId: String) = {
    DB.withTransaction { implicit session =>
      // these two operations should happen inside a transaction
      AccessTokens.filter(a => a.clientId === clientId && a.userId === userId).delete
      AccessTokens.insert(accessToken)
    }
  }

  def findToken(userId: Long, clientId: String): Option[AccessToken] = {
    DB.withTransaction { implicit session =>
      AccessTokens.filter(a => a.clientId === clientId && a.userId === userId).firstOption
    }
  }

  def findAccessToken(token: String): Option[AccessToken] = {
    DB.withTransaction { implicit session =>
      AccessTokens.filter(a => a.accessToken === token).firstOption
    }
  }

  def findRefreshToken(token: String): Option[AccessToken] = {
    DB.withTransaction { implicit session =>
      AccessTokens.filter(a => a.refreshToken === token).firstOption
    }
  }
}
