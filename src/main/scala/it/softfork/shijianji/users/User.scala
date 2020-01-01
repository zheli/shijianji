package it.softfork.shijianji.users

import java.util.UUID

import it.softfork.shijianji.utils.{HasDatabaseConfig, MyPostgresDriver}
import play.api.libs.json.{Json, OFormat}
import slick.basic.DatabaseConfig
import slick.lifted.MappedTo
import slick.sql.SqlProfile.ColumnOption.SqlType
import tech.minna.playjson.macros.jsonFlat

import scala.concurrent.ExecutionContext

// @jsonFlat case classes need to be defined before other class that uses these types.
@jsonFlat case class UserId(value: UUID) extends MappedTo[UUID]
@jsonFlat case class Password(value: String) extends MappedTo[String]

case class UserRequest(
  email: String, // use String for now
  password: Password
)

object UserRequest {
  implicit val formatter: OFormat[UserRequest] = Json.format
}

case class User(
  id: Option[Int] = None,
  userId: UserId,
  email: String, // use String for now
  password: Password,
)

object User {
  implicit val formatter: OFormat[User] = Json.format
  val testUser: User = User(userId = UserId(UUID.randomUUID()), email = "test@ha.com", password = Password("test123"))
}

trait UserRepository { self: HasDatabaseConfig[MyPostgresDriver] =>
  import profile.api._

  protected class UserTable(tag: Tag) extends Table[User](tag, "users") {
    def id = column[Int]("id", SqlType("SERIAL"), O.PrimaryKey, O.AutoInc)
    def userId = column[UserId]("user_id", O.Unique)
    def email = column[String]("email")
    def password = column[Password]("password")
    // I couldn't make "O.AutoInc" to work, so instead a SqlType("SERIAL") is used to create auto_incremental id

    def * = (id.?, userId, email, password) <> ((User.apply _).tupled, User.unapply)
  }
}

class UserRepositoryImpl(protected val dbConfig: DatabaseConfig[MyPostgresDriver])(implicit ec: ExecutionContext)
    extends UserRepository
    with HasDatabaseConfig[MyPostgresDriver] {
  import profile.api._

  private val users = TableQuery[UserTable]

  def setup = db.run(users.schema.createIfNotExists)

  def create(user:User) = {
    db.run(users += user)
  }
}
