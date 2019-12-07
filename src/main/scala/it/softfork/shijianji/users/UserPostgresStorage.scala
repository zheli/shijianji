package it.softfork.shijianji.users

import it.softfork.shijianji.models.Users
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

object UserPostgresStorage {
  val users = TableQuery[Users]

  def setup= DBIO.seq(users.schema.createIfNotExists)
}