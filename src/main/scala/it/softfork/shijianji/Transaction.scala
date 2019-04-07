package it.softfork.shijianji

import java.time.ZonedDateTime

sealed trait Transaction {
  val user: User
  val timestamp: ZonedDateTime
}

case class Trade(
  user: User,
  timestamp: ZonedDateTime,
  sellProduct: String, // Use String for now
  sellAmount: BigDecimal,
  buyProduct: String, // Use String for now
  buyAmount: BigDecimal,
  platform: String // Use String for now
) extends Transaction

case class Deposit(
  user: User,
  timestamp: ZonedDateTime,
  production: String, // Use String for now
  amount: BigDecimal,
  platform: String // Use String for now
) extends Transaction

case class Withdraw(
  user: User,
  timestamp: ZonedDateTime,
  production: String, // Use String for now
  amount: BigDecimal,
  platform: String // Use String for now
) extends Transaction
