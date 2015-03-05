package models.oauth2

import play.api.db.slick.Config.driver.simple._
import scala.slick.lifted.Tag

/**
 * Created by Alper on 29.01.2015.
 */
case class GrantType(id: Int, grantType: String)
class GrantTypes(tag: Tag) extends Table[GrantType](tag, Some("configuration") ,"grant_type") {
  def id = column[Int]("_id", O.PrimaryKey)
  def grantType = column[String]("grant_type")
  def * = (id, grantType) <> ((GrantType.apply _).tupled, GrantType.unapply _)
}
