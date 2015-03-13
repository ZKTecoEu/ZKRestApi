package models

import anorm.SqlParser._
import anorm.{NotAssigned, Pk, RowParser, _}
import play.api.Play.current
import play.api.db._
import utils.Conversion.pkToLong
import utils.{HashFactory, LoginCombinationHelper}

import scala.collection.JavaConverters._
import scala.collection.mutable._


case class Employee(
                      _id: Pk[Long] = NotAssigned,

                      first_name: String = "unknown name",
                     last_name: Option[String] = Option(""),
                     pin: String = "unknown pin",
                     birth_date: Option[Long] = Option(0),
                     address: Option[String] = Option(""),
                     gender: Option[Int] = Option(0),
                     phone: Option[String] = Option(""),
                     email: Option[String] = Option(""),
                     photo: Option[String] = Option(""),
                     national_id: Option[String] = Option("")
                     ) extends Entity {
  def asSeq(): Seq[(String, Any)] = Seq(
    "_id" -> pkToLong(_id),
    "first_name" -> first_name,
    "last_name" -> last_name,
    "pin" -> pin,
    "birth_date" -> birth_date,
    "address" -> address,
    "gender" -> gender,
    "phone" -> phone,
    "email" -> email,
    "photo" -> photo,
    "national_id" -> national_id
  )
}

object Employee extends EntityCompanion[Employee] {
  def fromParser(
                  _id: Pk[Long] = NotAssigned,
                  first_name: String = "unknown employee",
                  last_name: Option[String] = Option(""),
                  pin: String = "",
                  birth_date: Option[Long] = Option(0),
                  address: Option[String] = Option(""),
                  gender: Option[Int] = Option(0),
                  phone: Option[String] = Option(""),
                  email: Option[String] = Option(""),
                  photo: Option[String] = Option(""),
                  national_id: Option[String] = Option("")
                  ): Employee = {
    new Employee(
      _id,
      first_name,
      last_name,
      pin,
      birth_date,
      address,
      gender,
      phone,
      email,
      photo,
      national_id
    )
  }

  val tableName = "employee"

  val simpleParser: RowParser[Employee] = {
    get[Pk[Long]]("employee._id") ~
      get[String]("employee.first_name") ~
      get[Option[String]]("employee.last_name") ~
      get[String]("employee.pin") ~
      get[Option[Long]]("employee.birth_date") ~
      get[Option[String]]("employee.address") ~
      get[Option[Int]]("employee.gender") ~
      get[Option[String]]("employee.phone") ~
      get[Option[String]]("employee.email") ~
      get[Option[String]]("employee.photo") ~
      get[Option[String]]("employee.national_id") map {
      case _id ~ first_name ~ last_name ~ pin ~ birth_date ~ address ~ gender
        ~ phone ~ email ~ photo ~ national_id => fromParser(
        _id, first_name, last_name, pin, birth_date, address, gender, phone, email
        , photo, national_id
      )
    }
  }

  def findById(zoneName: String, _id: Long): Option[Employee] = {
    DB.withConnection { implicit connection =>
      SQL(
        """select * 
          from """ + zoneName + """.employee 
          where employee._id = {_id}"""
      ).on(
          '_id -> _id
        ).as(simpleParser.singleOpt)
    }
  }

  def checkData(login: LoginCombination, zoneName: String) = {
    val empId = findEmployeeId(login.pin,zoneName)
    var employee: Option[Employee] = null
    if (chechEnabled(empId,zoneName)) {
      if (checkEmployeeLogin(login, empId,zoneName)) {
        if (LoginCombinationHelper.checkCombinations(login, empId ,zoneName)) {
          employee = findById(zoneName, empId)
        }
      }
    }
    employee
  }

  def findEmployeeId(pin: String,zoneName: String): Long = {
    DB.withConnection { implicit connection =>
      SQL(
        "select _id from "+zoneName+".employee where pin = {pin}"
        ).on('pin -> pin)().map(row => row[Long]("_id")).toList.head

    }

  }

  def chechEnabled(_id: Long,zoneName: String): Boolean = {
    DB.withConnection { implicit connection =>
      SQL(
        "select enabled from "+zoneName+".entity where _id = {_id}"
        ).on('_id -> _id)().map(row => row[Int]("enabled")).toList.head == 1

    }
  }

  def checkEmployeeLogin(login: LoginCombination, empId: Long, zoneName:String) = {

    var validationList: MutableList[Boolean] = MutableList()

    if (!login.pwd.isEmpty) {
      validationList += HashFactory.check(login.pwd, findEmployeePWD(empId, 1 ,zoneName))
    }
    if (!login.fp.isEmpty) {
      validationList += checkLogin(empId, 2, login.fp,zoneName)
    }
    if (!login.card.isEmpty) {
      validationList += checkLogin(empId, 3, login.card,zoneName)
    }
    if (!login.face.isEmpty) {
      validationList += checkLogin(empId, 4, login.face,zoneName)
    }

    !mutableSeqAsJavaListConverter(validationList).asJava.contains(false) && validationList.size > 0

  }

  def checkLogin(id_employee: Long, id_login: Long, value: String , zoneName:String) = {
    val query =
      "select id_login from "+zoneName+".employee_login where "+
        "id_employee = {id_employee} and id_login = {id_login} "+
         "and value = {value}"


    DB.withConnection { implicit connection =>
      SQL(query).on('id_employee -> id_employee,
        'id_login -> id_login,
        'value -> value)().map(row => row[Long]("id_login")).toList.size > 0

    }
  }

  def findEmployeePWD(id_employee: Long, id_login: Long,zoneName:String) = {
    val query =
         "select value from "+zoneName+".employee_login where "+
         "id_employee = {id_employee} and id_login = {id_login}"


    DB.withConnection { implicit connection =>
      SQL(query).on('id_employee -> id_employee,
        'id_login -> id_login)().map(row => row[String]("value")).toList.head.toString

    }
  }
}
