package utils

import models.LoginCombination
import play.api.db._
import anorm._
import play.api.Play.current


/**
 * Created by alper on 27/11/14.
 */
object LoginCombinationHelper {

  final val PIN_WEIGHT = 1
  final val PASSWORD_WEIGHT = 2
  final val CARD_WEIGHT = 4
  final val FINGERPRINT_WEIGHT = 8
  final val FACE_RECOGNITION_WEIGHT = 16

  def checkCombinations(login:LoginCombination,id_employee:Long,zoneName:String):Boolean = {
    val login_combination = calculateWeight(login:LoginCombination)

    DB.withConnection{ implicit connection =>
      SQL(
        "select login_combination from "+zoneName+".employee_login_combination "+
          "where id_employee = {id_employee} and login_combination = {login_combination}"

      ).on(
         'id_employee -> id_employee,
         'login_combination -> login_combination
        )().map(row => row[Int]("login_combination")).toList.size>0
    }

  }

  def calculateWeight(login:LoginCombination):Int = {
     var value = 0

    if (!login.pin.isEmpty) {
      value += PIN_WEIGHT
    }

    if (!login.fp.isEmpty) {
      value += FINGERPRINT_WEIGHT
    }

    if (!login.pwd.isEmpty) {
      value += PASSWORD_WEIGHT
    }

    if (!login.card.isEmpty) {
      value += CARD_WEIGHT
    }

    if (!login.face.isEmpty) {
      value += FACE_RECOGNITION_WEIGHT
    }

    value
  }
}
