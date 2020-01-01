package it.softfork.shijianji.users

import java.util.UUID

import play.api.libs.json.{Json, OFormat}
import tech.minna.playjson.macros.jsonFlat
import slick.jdbc.PostgresProfile.api._
import slick.sql.SqlProfile.ColumnOption.SqlType

@jsonFlat case class Password(value: String) extends MappedTo[String]

case class UserRequest(
  email: String, // use String for now
  password: Password
)

object UserRequest {
  implicit val formatter: OFormat[UserRequest] = Json.format[UserRequest]
}

case class User(
  email: String, // use String for now
  password: Password,
  id: Option[Int] = None,
  uuid: UUID
)

object User {
  implicit val formatter: OFormat[User] = Json.format[User]
  val testUser: User = User("test@ha.com", Password("test123"), uuid = UUID.randomUUID())
}

class Users(tag: Tag) extends Table[User](tag, "USERS") {
  def email= column[String]("EMAIL")
  def password = column[Password]("PASSWORD")
  // I couldn't make "O.AutoInc" to work, so instead a SqlType("SERIAL") is used to create auto_incremental id
  def id = column[Int]("ID", SqlType("SERIAL"), O.PrimaryKey, O.AutoInc)
  def uuid = column[UUID]("UUID", O.Unique)

  def * = (email, password, id.?, uuid) <> ((User.apply _).tupled, User.unapply)
}
