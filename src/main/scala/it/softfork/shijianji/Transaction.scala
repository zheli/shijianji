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
    s"$updatedValue ${currency.value}"
  }

  def negate: Amount = Amount(value * -1, currency)

  def +(that: Amount) = {
    require(currency == that.currency)
    Amount(value + that.value, currency)
  }

  def -(that: Amount) = {
    require(currency == that.currency)
    Amount(value - that.value, currency)
  }
}

sealed trait Transaction extends Product {
  val user: User
  val timestamp: ZonedDateTime
  val fee: Option[Amount]
  val platform: String
  val externalId: Option[String]
  val comment: Option[String]
}

object Transaction {
  def balance(currency: Currency, transactions: Seq[Transaction]) = {
    val transactionsForCurrency = transactions.filter {
      case t: Trade => t.soldAmount.currency == currency || t.boughtAmount.currency == currency
      case non: NonTradingTransaction => non.amount.currency == currency
    }

    transactionsForCurrency
      .map {
        case ts: Trade if (ts.soldAmount.currency == currency) =>
          ts.fee match {
            case Some(fee) if fee.currency == currency => ts.soldAmount.negate - fee
            case _ => ts.soldAmount.negate
          }
        case tb: Trade if (tb.boughtAmount.currency == currency) =>
          tb.fee match {
            case Some(fee) if fee.currency == currency => tb.boughtAmount - fee
            case _ => tb.boughtAmount
          }
        case non: NonTradingTransaction =>
          non.fee match {
            case Some(fee) => non.amount - fee
            case _ => non.amount
          }
      }
      .fold(Amount(BigDecimal(0), currency))((a1: Amount, a2: Amount) => a1 + a2)
  }
}

case class Trade(
  user: User,
  timestamp: ZonedDateTime,
  soldAmount: Amount,
  boughtAmount: Amount,
  fee: Option[Amount],
  platform: String, // Use String for now
  comment: Option[String],
  externalId: Option[String]
) extends Transaction {
  def buyingPrice = soldAmount.value / boughtAmount.value
  def sellingPrice = boughtAmount.value / soldAmount.value
}

sealed trait NonTradingTransaction extends Transaction {
  def amount: Amount
  def toCsv: String = s"$timestamp, ${amount.value}, ${amount.currency.value}, $platform"
}

case class Deposit(
  user: User,
  timestamp: ZonedDateTime,
  amount: Amount,
  fee: Option[Amount],
  platform: String, // Use String for now
  comment: Option[String],
  externalId: Option[String]
) extends NonTradingTransaction {
  require(amount.value >= 0, "Deposit amount should be positive!")
}

case class Withdraw(
  user: User,
  timestamp: ZonedDateTime,
  amount: Amount,
  fee: Option[Amount],
  platform: String, // Use String for now
  comment: Option[String],
  externalId: Option[String]
) extends NonTradingTransaction {
  require(amount.value <= 0, "Withdraw amount should be negative!")
}
