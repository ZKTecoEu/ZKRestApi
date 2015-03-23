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
                     national_id: Option[String] = Option(""),
                     department: Option[String] = Option(""),
                     enabled: Option[Boolean] = Option(true)
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
    "national_id" -> national_id,
    "department" -> department,
    "enabled" -> enabled
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
                  national_id: Option[String] = Option(""),
                  department: Option[String] = Option(""),
                  enabled: Option[Boolean] = Option(true)
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
      national_id,
      department,
      enabled
    )
  }

  val tableName = "employee"

  //TODO gmerino: this enables SQL injection and should be fixed, solution proposals can be found here:
  // http://stackoverflow.com/questions/29028122/best-way-to-pass-the-schema-name-as-a-variable-to-a-query
  /**
   * Retrieves the basic query to get employee data. This information can be parsed with simpleParser
   * @param zoneName
   * @return
   */
  private def getEmployeeDefaultQuery(zoneName: String) : String =
    """
       SELECT em.*, en.enabled  as enabled, eg.name as department
       FROM """+zoneName+""".employee em,  """+zoneName+""".entity en,  """+zoneName+""".entity_group eg,  """+zoneName+""".entity_hierarchy eh
       WHERE em._id = en._id
       AND em._id = eh.id_entity
       AND eg._id = eh.id_parent
    """

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
      get[Option[String]]("employee.national_id") ~
      get[Option[String]]("department") ~
      get[Option[Int]]("enabled") map {
      case _id ~ first_name ~ last_name ~ pin ~ birth_date ~ address ~ gender
        ~ phone ~ email ~ photo ~ national_id ~ department ~ enabled => fromParser(
        _id, first_name, last_name, pin, birth_date, address, gender, phone, email
        , photo, national_id, department, Option(enabled == 1)
      )
    }
  }


  /**
   * Retrieves an employee
   * @param zoneName
   * @param _id
   * @return
   */
  def getById(zoneName: String, _id: Long): Option[Employee] = {
    DB.withConnection { implicit connection =>
      SQL(
        getEmployeeDefaultQuery(zoneName)+" AND em._id = {_id}"
      ).on(
          '_id -> _id
        ).as(simpleParser.singleOpt)
    }
  }

  def checkData(zoneName: String, login: LoginCombination): Option[Employee] = {
    val empId = findEmployeeId(login.pin, zoneName)
    var employee: Option[Employee] = null
    if (isEnabled(zoneName, empId)) {
      if (checkEmployeeLogin(zoneName, login, empId)) {
        if (LoginCombinationHelper.checkCombinations(login, empId, zoneName)) {
          employee = getById(zoneName, empId)
        }
      }
    }
    employee
  }

  /**
   * Find an employee id based on the pin
   * @param zoneName
   * @param pin
   * @return
   */
  private def findEmployeeId(zoneName: String, pin: String): Long = {
    DB.withConnection { implicit connection =>
      SQL(
        "select _id from " + zoneName + ".employee where pin = {pin}"
      ).on('pin -> pin)().map(row => row[Long]("_id")).toList.head

    }

  }

  /**
   * Checks if the employee is enabled
   * @param zoneName
   * @param _id
   * @return
   */
  def isEnabled(zoneName: String, _id: Long): Boolean = {
    DB.withConnection { implicit connection =>
      SQL(
        "select enabled from " + zoneName + ".entity where _id = {_id}"
      ).on('_id -> _id)().map(row => row[Int]("enabled")).toList.head == 1

    }
  }

  /**
   *
   * @param zoneName
   * @param login
   * @param empId
   * @return
   */
  private def checkEmployeeLogin(zoneName: String, login: LoginCombination, empId: Long) = {

    var validationList: MutableList[Boolean] = MutableList()

    if (!login.pwd.isEmpty) {
      validationList += HashFactory.check(login.pwd, findEmployeePWD(zoneName, empId, 1))
    }
    if (!login.fp.isEmpty) {
      validationList += checkLogin(zoneName, empId, 2, login.fp)
    }
    if (!login.card.isEmpty) {
      validationList += checkLogin(zoneName, empId, 3, login.card)
    }
    if (!login.face.isEmpty) {
      validationList += checkLogin(zoneName, empId, 4, login.face)
    }

    !mutableSeqAsJavaListConverter(validationList).asJava.contains(false) && validationList.size > 0

  }

  def checkLogin(zoneName: String, id_employee: Long, id_login: Long, value: String) = {
    val query =
      "select id_login from " + zoneName + ".employee_login where " +
        "id_employee = {id_employee} and id_login = {id_login} " +
        "and value = {value}"


    DB.withConnection { implicit connection =>
      SQL(query).on('id_employee -> id_employee,
        'id_login -> id_login,
        'value -> value)().map(row => row[Long]("id_login")).toList.size > 0

    }
  }

  private def findEmployeePWD(zoneName: String, id_employee: Long, id_login: Long): String = {
    val query =
      "select value from " + zoneName + ".employee_login where " +
        "id_employee = {id_employee} and id_login = {id_login}"

    DB.withConnection { implicit connection =>
      SQL(query).on('id_employee -> id_employee,
        'id_login -> id_login)().map(row => row[String]("value")).toList.head.toString

    }
  }

  /**
   * Gets a list from employees
   * @param zoneName
   * @return
   */
  def getEmployeeList(zoneName: String): List[Employee] /*:Seq[Employee]*/ = {
    DB.withConnection { implicit connection =>
      SQL(  getEmployeeDefaultQuery(zoneName)+" ORDER BY pin").as(simpleParser *)
    }
  }

}
