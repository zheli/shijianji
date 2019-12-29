package it.softfork.shijianji.users

import java.util.UUID

import play.api.libs.json.Json
import slick.jdbc.PostgresProfile.api._

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
