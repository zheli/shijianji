package it.softfork.shijianji.users

import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

object UserPostgresStorage {
  val users = TableQuery[Users]

  def setup= DBIO.seq(users.schema.createIfNotExists)
  def teardown = users.schema.dropIfExists
}

