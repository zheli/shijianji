package it.softfork.shijianji

import java.time.ZonedDateTime

import play.api.libs.json.JsValue
import tech.minna.playjson.macros.jsonFlat

@jsonFlat case class Currency(value: String) extends AnyVal

case class Amount(
  value: BigDecimal,
  currency: Currency
)

sealed trait Transaction {
  val user: User
  val timestamp: ZonedDateTime
  val fee: Option[Amount]
  val platform: String
}

case class Trade(
  user: User,
  timestamp: ZonedDateTime,
  soldAmount: Amount,
  boughtAmount: Amount,
  fee: Option[Amount],
  platform: String, // Use String for now
  extraJsonData: Option[JsValue]
) extends Transaction {
  def buyingPrice = soldAmount.value / boughtAmount.value
  def sellingPrice = boughtAmount.value / soldAmount.value
}

sealed trait NonTradingTransaction extends Transaction {
  def amount: Amount
}

case class Deposit(
  user: User,
  timestamp: ZonedDateTime,
  amount: Amount,
  fee: Option[Amount],
  platform: String // Use String for now
) extends NonTradingTransaction

case class Withdraw(
  user: User,
  timestamp: ZonedDateTime,
  amount: Amount,
  fee: Option[Amount],
  platform: String // Use String for now
) extends NonTradingTransaction
