package models.oauth2

import models.oauth2._

import scala.slick.lifted.TableQuery

/**
 * Created by Alper on 29.01.2015.
 */
private[models] trait DAO {
  val Devices = TableQuery[Devices]
  val Users = TableQuery[Users]
  val GrantTypes = TableQuery[GrantTypes]
  val DeviceGrantTypes = TableQuery[DeviceGrantTypes]
  val AccessTokens = TableQuery[AccessTokens]
  val AuthCodes = TableQuery[AuthCodes]
}
