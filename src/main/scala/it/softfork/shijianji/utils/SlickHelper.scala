package it.softfork.shijianji.utils

import slick.basic.{BasicProfile, DatabaseConfig}

trait HasDatabaseConfig[P <: BasicProfile] {
  /** The Slick database configuration. */
  protected val dbConfig: DatabaseConfig[P] // field is declared as a val because we want a stable identifier.
  /** The Slick profile extracted from `dbConfig`. */
  protected final lazy val profile: P = dbConfig.profile // field is lazy to avoid early initializer problems.
  /** The Slick database extracted from `dbConfig`. */
  protected final def db: P#Backend#Database = dbConfig.db
}
