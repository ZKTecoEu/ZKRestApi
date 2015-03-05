package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import java.sql.ResultSet

trait Entity {
  val _id: Pk[Long]
  
  def asSeq(): Seq[(String,Any)]
}

trait EntityCompanion[A<:Entity] {

  lazy val findCommand: String = "select %s from "

  val simpleParser: RowParser[A]

  def findAll(rs: ResultSet): Iterator[Any] = new Iterator[Any] {
     def hasNext = rs.next()
     def next() = rs.getString(1)
  }
 
}

