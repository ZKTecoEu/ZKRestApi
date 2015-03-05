package utils

import java.sql.ResultSet

import formatters.DataFormatter.JsonDataFormatter
import play.api.db.DB
import play.api.Play.current
import anorm._
import play.api.libs.json.JsValue

/**
 * Created by alper on 28/11/14.
 */
//TODO optimize insert,delete and update functions!!!
object DBHelper {

  final val SET_PATH = "SET search_path TO "
  final val findCommand = "select * from "

  def insert(anyQuery:SimpleSql[Row],opQuery:SimpleSql[Row],insertOpLog:Boolean) = {
    DB.withConnection{ implicit connection =>
      connection.setAutoCommit(false)
      try{
        anyQuery.executeInsert()
        if(insertOpLog){
          opQuery.executeInsert()
        }
        connection.commit()
      }
      catch {
        case e: Exception => connection.rollback()
      }
    }
  }

  /**
   * This function executes generic queries which don't have any parameters.
   * According to result set,jsonList is created and returned...
   *
   * @param query
   * @param zoneName
   * @return jsonList
   */
  def executeQueryWithoutParams(query:String,zoneName:String) = {
    val conn = DB.getConnection()
    val stmt = conn.createStatement
    stmt.executeUpdate(SET_PATH+zoneName)
    val rs = stmt.executeQuery(query)
    val jsonList = convertResultSetToList(rs)
    rs.close()
    stmt.close()
    conn.close()
    jsonList
  }

  /**
   * This function executes generic queries which have namedParameters.
   * namedParameters are parsed from json body and sent this function.
   *
   * @param query
   * @param zoneName
   * @param dataMap
   * @return
   */
  def executeQueryWithParams(query:String,zoneName:String,dataMap:Map[Int,Any]) = {
    val conn = DB.getConnection()
    var ps = conn.prepareStatement(SET_PATH+zoneName)
    ps.executeUpdate()
    ps = conn.prepareStatement(query)
    dataMap.foreach {
      case (k, v) => {
         v match {
           case a: String => ps.setString(k, a)
           case a: Long => ps.setLong(k, a)
         }
      }
    }
    val rs = ps.executeQuery()
    val jsonList = convertResultSetToList(rs)
    rs.close()
    ps.close()
    conn.close()
    jsonList
  }

  /**
   *
   * @param table_name
   * @param zone_name
   * @param id
   * @return
   */
  def showData(table_name:String,zone_name:String,id:Long) = {
    val conn = DB.getConnection()
    val stmt = conn.createStatement
    val rs = stmt.executeQuery(findCommand + zone_name + "." + table_name + " where _id=" + id)
    val jsonList = convertResultSetToList(rs)
    rs.close()
    stmt.close()
    conn.close()
    jsonList
  }

  /**
   *
   * @param table_name
   * @param zone_name
   * @return
   */
  def showAllData(table_name:String,zone_name:String) = {
    val conn = DB.getConnection()
    val stmt = conn.createStatement
    val rs = stmt.executeQuery(findCommand + zone_name + "." + table_name)
    val jsonList = convertResultSetToList(rs)
    rs.close()
    stmt.close()
    conn.close()
    jsonList
  }

  /**
   * This function generates jsonList from result set.
   *
   * @param rs
   * @return jsonList
   */
  def convertResultSetToList(rs:ResultSet) = {
    val md = rs.getMetaData
    val columns = md.getColumnCount
    val jsonList = scala.collection.mutable.MutableList[JsValue]()
    while(rs.next()){
      val dataMap = scala.collection.mutable.Map[String, Any]()
      for(i <- 1 to columns){
        dataMap += md.getColumnName(i) -> rs.getObject(i)
      }
      jsonList += JsonDataFormatter.writes(dataMap.toMap)
    }
    jsonList
  }





}
