package oauth2

/**
 * Created by Alper on 29.01.2015.
 */

import models.User
import models.oauth2._
import utils.HashFactory

import scala.concurrent.Future
import scalaoauth2.provider.{ClientCredential, AuthInfo, DataHandler}
import java.util.Date
import java.sql.Timestamp

class OauthDataHandler extends DataHandler[User] {

  def findUser(username: String, password: String): Future[Option[User]] = {
    Future.successful(Users.findUser(username, password))
  }

  def createAccessToken(authInfo: AuthInfo[User]): Future[scalaoauth2.provider.AccessToken] = {
    val accessTokenExpiresIn = 60 * 60 // 1 hour
    val now = new Date()
    val createdAt = new Timestamp(now.getTime)
    val refreshToken = Some(HashFactory.generateToken())
    val accessToken = HashFactory.generateToken()

    val tokenObject = AccessToken(accessToken, refreshToken, authInfo.user._id, authInfo.scope, accessTokenExpiresIn, createdAt, authInfo.clientId.get)
    AccessTokens.deleteExistingAndCreate(tokenObject, authInfo.user._id, authInfo.clientId.get)
    Future.successful(scalaoauth2.provider.AccessToken(accessToken, refreshToken, authInfo.scope, Some(accessTokenExpiresIn.toLong), now))
  }

  def getStoredAccessToken(authInfo: AuthInfo[User]): Future[Option[scalaoauth2.provider.AccessToken]] = {
    Future.successful(AccessTokens.findToken(authInfo.user._id, authInfo.clientId.get) map { a =>
      scalaoauth2.provider.AccessToken(a.accessToken, a.refreshToken, a.scope, Some(a.expiresIn.toLong), a.createdAt)
    })
  }

  def refreshAccessToken(authInfo: AuthInfo[User], refreshToken: String): Future[scalaoauth2.provider.AccessToken] = {
    createAccessToken(authInfo)
  }

  def findAccessToken(token: String): Future[Option[scalaoauth2.provider.AccessToken]] = {
    Future.successful(AccessTokens.findAccessToken(token) map { a =>
      scalaoauth2.provider.AccessToken(a.accessToken, a.refreshToken, a.scope, Some(a.expiresIn.toLong), a.createdAt)
    })
  }

  def findAuthInfoByAccessToken(accessToken: scalaoauth2.provider.AccessToken): Future[Option[AuthInfo[User]]] = {
    Future.successful(AccessTokens.findAccessToken(accessToken.token) map { a =>
      val user = Users.getById(a.userId).get
      AuthInfo(user, Option(a.clientId), a.scope, Some(""))
    })
  }

  def findAuthInfoByRefreshToken(refreshToken: String): Future[Option[AuthInfo[User]]] = {
    Future.successful(AccessTokens.findRefreshToken(refreshToken) map { a =>
      val user = Users.getById(a.userId).get
      AuthInfo(user, Option(a.clientId), a.scope, Some(""))
    })
  }

  def findAuthInfoByCode(code: String): Future[Option[AuthInfo[User]]] = {
    Future.successful(AuthCodes.find(code) map { a =>
      val user = Users.getById(a.userId).get
      AuthInfo(user, Option(a.clientId), a.scope, a.redirectUri)
    })
  }

  def validateClient(clientCredential: ClientCredential, grantType: String): Future[Boolean] = {
    Future.successful(Devices.validate(clientCredential,grantType))
  }

  def findClientUser(clientCredential: ClientCredential, scope: Option[String]): Future[Option[User]] = {
    //Correct this logic!!
    Future.successful(Option(new User(1,"alper","alper.ytu7.gmail.com","alper48", models.Permission.valueOf("Administrator"))))
  }
}
