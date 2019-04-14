package it.softfork.shijianji

import java.util.UUID

case class UserId(value: UUID) extends AnyVal

case class User(
  id: UserId,
  email: String // use String for now
)
