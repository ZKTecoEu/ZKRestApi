package models

import anorm.SqlParser._
import anorm._
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.DB
import play.api.Play.current

import scala.collection.mutable.Seq

/**
 * Created by Alper on 27.01.2015.
 */
case class Client(_id:Long,code:String,description:String,schema_name:String)
  {
    def asSeq(): Seq[(String, Any)] = Seq(
      "_id" -> _id,
      "code" -> code,
      "description" -> description,
      "schema_name" -> schema_name
    )
  }

case class ClientForm(clientId:Long,code:String,description:String,zoneName:String)

object Client {

    val simpleParser = {
        get[Long]("client._id") ~
        get[String]("client.code") ~
        get[String]("client.description") ~
        get[String]("client.schema_name")  map {
        case _id~code~description~schema_name => Client(_id, code , description , schema_name)
      }
    }

    val clientForm: Form[ClientForm] = Form(
      mapping(
        "Client ID" -> longNumber,
        "Code" -> nonEmptyText,
        "Description" -> nonEmptyText,
        "Zone Name" -> nonEmptyText
      )(ClientForm.apply)(ClientForm.unapply)
    )

    def findAll = {
      DB.withConnection { implicit connection =>
        SQL("select * from configuration.client").as(Client.simpleParser *).toList
      }
    }

    def deleteUserfromDB(_id:Long) = {

      DB.withConnection { implicit connection =>
        SQL("delete from configuration.client where _id = {_id}").on(
          '_id -> _id
        ).execute()
      }

    }

    def createFromForm(client: ClientForm) = {
      DB.withConnection { implicit connection =>
        SQL(
          """
          insert into configuration.client values (
             {_id}, {code}, {description} , {schema_name}
          )
          """
        ).on(
            '_id -> client.clientId,
            'code -> client.code,
            'description -> client.description,
            'schema_name -> client.zoneName
          ).executeUpdate()
      }
    }

  }