package models.oauth2

import play.api.db.slick.Config.driver.simple._
import scala.slick.lifted.Tag
import play.api.db.slick.DB
import play.api.Play.current
import scalaoauth2.provider.{ClientCredential}

/**
 * Created by Alper on 29.01.2015.
 */
case class Device(id: String, secret: Option[String], redirectUri: Option[String], scope: Option[String])
class Devices(tag: Tag) extends Table[Device](tag,Some("configuration"),"device") {
  def id = column[String]("_id", O.PrimaryKey)
  def secret = column[Option[String]]("secret")
  def redirectUri = column[Option[String]]("redirect_uri")
  def scope = column[Option[String]]("scope")
  def * = (id, secret, redirectUri, scope) <> ((Device.apply _).tupled, Device.unapply _)
}

object Devices extends DAO {
  def validate(credits:ClientCredential, grantType: String): Boolean = {
    DB.withTransaction { implicit session =>
      val check = for {
        ((c, cgt), gt) <- Devices innerJoin DeviceGrantTypes on (_.id === _.deviceId) innerJoin GrantTypes on (_._2.grantTypeId === _.id)
        if c.id === credits.clientId && c.secret === credits.clientSecret && gt.grantType === grantType
      } yield 0
      check.firstOption.isDefined
    }
  }

  def findById(id: String): Option[Device] = {
    DB.withTransaction { implicit session =>
      Devices.filter(c => c.id === id).firstOption
    }
  }
}
