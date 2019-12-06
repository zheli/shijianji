package it.softfork.shijianji.models

import java.util.UUID

import play.api.libs.json.Json
//import slick.basic._
//import slick.jdbc.JdbcProfile
//import slick.jdbc.H2Profile.api._
import slick.jdbc.PostgresProfile.api._

//import scala.concurrent.Future

case class User(
  email: String, // use String for now
  id: Option[Int] = None,
  uuid: UUID
)

object User {
  implicit val userFormat = Json.format[User]
}

class Users(tag: Tag) extends Table[User](tag, "USERS") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def uuid = column[UUID]("UUID", O.Unique)
  def email= column[String]("EMAIL")

  def * = (email, id.?, uuid) <> ((User.apply _).tupled, User.unapply)
}

//object Users {
//  def setup(db: Database) = {
//    val users = TableQuery[Users]
//    db.run(
//      DBIO.seq(
//        users.schema.create
//      )
//    )
//  }
//}

//class UserRepository(dbConfig: DatabaseConfig[JdbcProfile]) {
//  import dbConfig._
//  import profile.api._
//
//  private class UserTable(tag: Tag) extends Table[User](tag, "USERS") {
//    def id = column[UUID]("USER_ID", O.PrimaryKey) // This is the primary key column
//    def email = column[String]("USER_EMAIL")
//
//    override def * = (id, email).<>((User.apply _).tupled, User.unapply)
//  }
//
//  private val user = TableQuery[UserTable]
//
//  def create(email: String): Future[User] = {
//    val id = UUID.randomUUID()
//    db.run {
//      (user.map(p => (id, p.email))
//      returning user.map(_.id)
//      into ((idEmail, id) => User(id, idEmail._2))
//      ) += (id, email)
//    }
//  }
//
//  def list(): Future[Seq[User]] = db.run {
//    user.result
//  }
//
//}

