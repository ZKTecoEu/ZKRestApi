package models.oauth2

import play.api.db.slick.Config.driver.simple._
import scala.slick.lifted.Tag


/**
 * Created by Alper on 29.01.2015.
 */
case class DeviceGrantType(clientId: String, grantTypeId: Int)
class DeviceGrantTypes(tag: Tag) extends Table[DeviceGrantType](tag, Some("configuration") ,"device_grant_type") {
  def deviceId = column[String]("device_id")
  def grantTypeId = column[Int]("grant_type_id")
  def * = (deviceId, grantTypeId) <> ((DeviceGrantType.apply _).tupled, DeviceGrantType.unapply _)
  val pk = primaryKey("pk_device_grant_type", (deviceId, grantTypeId))
}
