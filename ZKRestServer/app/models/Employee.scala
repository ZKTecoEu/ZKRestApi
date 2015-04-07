package models

import anorm.SqlParser._
import anorm.{NotAssigned, RowParser, _}
import play.api.Logger
import play.api.Play.current
import play.api.db._
import utils.Conversion.pkToLong
import utils.{HashFactory, LoginCombinationHelper, SQLHelper}

import scala.collection.JavaConverters._
import scala.collection.mutable._


case class Employee(
                     _id: Long,
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
                     )

object Employee {
  def fromParser(
                  _id: String ,
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
      _id.toLong,
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


  val simpleParser: RowParser[Employee] = {
    get[Long]("employee._id") ~
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
        _id.toString, first_name, last_name, pin, birth_date, address, gender, phone, email
        , photo, national_id, department, enabled match {
          case Some(enabled) => Option(enabled == 1)
          case None => Option(false)
        }
      )
    }
  }


  /**
   * Retrieves the basic query to get employee data. This information can be parsed with simpleParser
   * @param zoneName
   * @return
   */
  private def getEmployeeDefaultQuery(zoneName: String): String =
    """
       SELECT em.*, en.enabled  as enabled, eg.name as department
       FROM """ + validateZoneName(zoneName) + """.employee em,  """ + validateZoneName(zoneName) + """.entity en,  """ + validateZoneName(zoneName) + """.entity_group eg,  """ + validateZoneName(zoneName) + """.entity_hierarchy eh
       WHERE em._id = en._id
       AND em._id = eh.id_entity
       AND eg._id = eh.id_parent """

  /* TODO gmerino: this enables SQL injection and should be fixed, solution proposals can be found here:
   http://stackoverflow.com/questions/29028122/best-way-to-pass-the-schema-name-as-a-variable-to-a-query */
  private def validateZoneName(zoneName: String): String = zoneName


  /**
   * Retrieves an employee
   * @param zoneName
   * @param _id
   * @return
   */
  def getById(zoneName: String, _id: Long): Option[Employee] = {
    DB.withConnection { implicit connection =>
      SQL(
        getEmployeeDefaultQuery(zoneName) + " AND em._id = {_id}"
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
      SQL(getEmployeeDefaultQuery(zoneName) + " ORDER BY pin").as(simpleParser *)
    }
  }

  def updateEmployeePhoto(zoneName: String, employee: Employee) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          UPDATE """ + validateZoneName(zoneName) + """.employee
          SET
          photo = {photo}
          WHERE _id = {_id}""").on(
          '_id -> employee._id,
          'photo -> employee.photo.getOrElse("")
        ).executeUpdate()
    }
  }

  def updateEmployee(zoneName: String, employee: Employee): Boolean = {
    DB.withConnection { implicit connection =>

      var auxEnabled = 1

      employee.enabled match {
        case Some(true) => auxEnabled = 1
        case Some(false) => auxEnabled = 0
        case None => auxEnabled = -1
      }
      if (auxEnabled >= 0) {
        SQL(
          """
          UPDATE """ + validateZoneName(zoneName) + """.entity
          SET
          enabled = {enabled}
          WHERE _id = {_id}""").on(
            '_id -> employee._id,
            'enabled -> auxEnabled
          ).executeUpdate()
      }

//      Logger.info("Antes de ejecutar el update for Mr " + employee.last_name.get)
      SQL(
        """
          UPDATE """ + validateZoneName(zoneName) + """.employee
          SET
          first_name = {first_name},
          last_name = {last_name},
          pin = {pin},
          birth_date = {birth_date},
          address = {address},
          gender = {gender},
          phone = {phone},
          email = {email},
          photo = {photo},
          national_id = {national_id}
          WHERE _id = {_id}""").on(
          '_id -> employee._id,
          'first_name -> employee.first_name,
          'last_name -> employee.last_name.orNull,
          'pin -> employee.pin,
          'birth_date -> employee.birth_date.getOrElse(0l),
          'address -> employee.address.getOrElse(""),
          'gender -> employee.gender.getOrElse(0),
          'phone -> employee.phone.getOrElse(""),
          'email -> employee.email.getOrElse(""),
          'photo -> employee.photo.getOrElse(""),
          'national_id -> employee.national_id.getOrElse("")
        ).executeUpdate()


      Logger.info("Despues de ejecutar el update")
      //TODO Department
      true
    }

  }

}
