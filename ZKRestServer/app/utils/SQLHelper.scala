package utils

import anorm._
import models.AttendanceLog
import org.joda.time.DateTime

import scala.reflect.runtime.currentMirror
import scala.reflect.runtime.universe._
import scala.util.Random

/**
 * Created by alper on 3/12/14.
 */
object SQLHelper {

  def insertAttendanceQuery(attendanceLog: AttendanceLog, zoneName: String) = {
    SQL("insert into " + zoneName + ".attendance_log(_id,id_attendance_event,id_employee," +
      "login_combination,date,status,annotation,processed,id_device) values({_id},{id_attendance_event}," +
      "{id_employee},{login_combination},{date},{status},{annotation},{processed},{id_device})").on(namedParameters(generateMap(attendanceLog)): _*)
  }

  def insertOpLogQuery(any: Any, zoneName: String, table: String) = {
    SQL(
      "insert into " + zoneName + ".core_operation_log(_id,date,action,info) values({_id},{date},{action},{info})"
    ).on('_id -> Random.nextLong(),
        'date -> DateTime.now().getMillis,
        'action -> 1,
        'info -> generateInfoFromMap(generateMap(any), table))
  }

  /**
   * This function generates info column for core_operation_log table
   * @param map
   * @param tableName
   * @return info for core_operation_log
   */
  def generateInfoFromMap(map: Map[String, Any], tableName: String): String = {
    var builder: StringBuilder = new StringBuilder()
    builder.append(tableName)
    map.foreach {
      case (k, v) => builder.append(k + "=" + v + ";")
    }
    builder.toString()
  }

  /**
   * This function takes object and generate map from its variable names and variables values
   * It is very useful for setting anorm variables but you need to add .toSeq in order to use setting anorm variables
   * I did not add .toSeq this function end in order to use generating info of core_operation_log
   * @param any
   * @return Map[String,Any]
   */
  def generateMap(any: Any): Map[String, Any] = {
    val r = currentMirror.reflect(any)
    r.symbol.typeSignature.members.toStream
      .collect { case s: TermSymbol if !s.isMethod => r.reflectField(s) }
      .map(r => r.symbol.name.toString.trim -> findvalue(r.get))
      .toMap
  }

  /**
   * This function helper for generateMap function
   * @param r
   * @return Any
   */
  def findvalue(r: Any): Any = r match {
    case _: String => r.toString
    case _: Int => r.toString.toInt
    case _: Long => r.toString.toLong
    case _: Boolean => r.toString.toBoolean match {
      case true => 1
      case false => 0
    }
  }

  /**
   * This function helper for ParameterValue creation
   * @param r
   * @return
   */
  def getParameterValue(r: Any): ParameterValue = r match {
    case _: String => ParameterValue.toParameterValue(r.toString)
    case _: Int => ParameterValue.toParameterValue(r.toString.toInt)
    case _: Long => ParameterValue.toParameterValue(r.toString.toLong)
  }

  def namedParameters(generatedMap: Map[String, Any]) = {
    generatedMap map {
      case (k, v) => NamedParameter(k, getParameterValue(v))
    } toSeq
  }
}
