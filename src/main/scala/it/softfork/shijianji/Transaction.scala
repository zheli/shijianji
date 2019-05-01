package it.softfork.shijianji

import java.time.ZonedDateTime

import play.api.libs.json.JsValue
import tech.minna.playjson.macros.jsonFlat

@jsonFlat case class Currency(value: String) extends AnyVal

object Currency {
  val fiats: Set[String] = Set(
    "EUR",
    "USD"
  )
}

case class Amount(
  value: BigDecimal,
  currency: Currency
) {
  override def toString: String = {
    val updatedValue = {
      if (Currency.fiats.contains(currency.value)) {
        value.setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
      } else
        value
    }
    s"$updatedValue ${ currency.value }"
  }
}

sealed trait Transaction {
  val user: User
  val timestamp: ZonedDateTime
  val fee: Option[Amount]
  val platform: String
  val externalId: String
}

case class Trade(
  user: User,
  timestamp: ZonedDateTime,
  soldAmount: Amount,
  boughtAmount: Amount,
  fee: Option[Amount],
  platform: String, // Use String for now
  externalId: String,
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
  platform: String, // Use String for now
  externalId: String
) extends NonTradingTransaction {
   require(amount.value >= 0, "Deposit amount should be positive!")
 }

case class Withdraw(
  user: User,
  timestamp: ZonedDateTime,
  amount: Amount,
  fee: Option[Amount],
  platform: String, // Use String for now
  externalId: String
) extends NonTradingTransaction {
  require(amount.value <= 0, "Withdraw amount should be negative!")
}
