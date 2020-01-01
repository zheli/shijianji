package it.softfork.shijianji.integrations.coinbasepro

import java.time.ZonedDateTime
import java.util.UUID

import it.softfork.shijianji._
import it.softfork.shijianji.users.User
import play.api.libs.json.JsonConfiguration.Aux
import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json.{Format, Json, JsonConfiguration, Reads}
import tech.minna.playjson.macros.{json, jsonFlat}

case class CoinbaseProduct(
  id: ProductId,
  baseCurrency: Currency,
  quoteCurrency: Currency,
  baseMinSize: String,
  baseMaxSize: String,
  quoteIncrement: String
)

case class Fill(
  tradeId: TradeId,
  productId: ProductId,
  price: Price,
  size: Size,
  orderId: UUID,
  liquidity: String, // Use string for now
  createdAt: ZonedDateTime,
  fee: Option[BigDecimal],
  settled: Boolean,
  side: String // Use string for now
) {
  val boughtCurrency: String = productId.value.split("-").head
  val soldCurrency: String = productId.value.split("-").tail.head
  val soldAmount = Amount(value = price.value * size.value, currency = Currency(soldCurrency))
  val boughtAmount = Amount(value = size.value, currency = Currency(boughtCurrency))
  val externalId = s"trade:${tradeId.value}"
}

object Fill {

  def toTrade(user: User, fill: Fill): Trade = {
    val fee = fill.fee.map(Amount(_, fill.soldAmount.currency))

    Trade(
      user = user,
      timestamp = fill.createdAt,
      soldAmount = fill.soldAmount,
      boughtAmount = fill.boughtAmount,
      fee = fee,
      platform = "CoinbasePro", // Use String for now
      comment = None,
      externalId = Some(fill.externalId)
    )
  }
}

@jsonFlat case class AccountId(value: UUID) extends AnyVal
@jsonFlat case class TradeId(value: Int) extends AnyVal
@jsonFlat case class ProductId(value: String) extends AnyVal
@jsonFlat case class OrderId(value: UUID) extends AnyVal

case class Price(value: BigDecimal) extends AnyVal

object Price {
  implicit val priceReads: Reads[Price] = Reads[Price] { json =>
    json.validate[String].map(s => Price(BigDecimal(s)))
  }
}

case class Size(value: BigDecimal) extends AnyVal

object Size {
  implicit val sizeReads: Reads[Size] = Reads[Size] { json =>
    json.validate[String].map(s => Size(BigDecimal(s)))
  }
}

case class Account(
  profileId: UUID,
  id: AccountId,
  currency: Currency,
  balance: BigDecimal,
  available: BigDecimal,
  hold: BigDecimal
)

object Account {
  implicit val config: Aux[Json.MacroOptions] = JsonConfiguration(SnakeCase)
  implicit val formatter: Format[Account] = Json.format[Account]
}

@jsonFlat case class AccountActivityId(value: Int) extends AnyVal

case class AccountActivity(
  id: AccountActivityId,
  createdAt: ZonedDateTime,
  amount: BigDecimal,
  balance: BigDecimal,
  `type`: String // Use String for now
  // details: Details Skip details for now
) {
  def externalId = s"activity:${id.value}"
}

object AccountActivity {
  implicit val config: Aux[Json.MacroOptions] = JsonConfiguration(SnakeCase)
  implicit val formatter: Format[AccountActivity] = Json.format[AccountActivity]

  def transfers(user: User, account: Account, activities: Seq[AccountActivity]): Seq[NonTradingTransaction] = {
    activities
      .filter(_.`type` == "transfer")
      .map { activity =>
        if (activity.amount >= 0) {
          Deposit(
            user,
            activity.createdAt,
            Amount(activity.amount, account.currency),
            fee = None,
            platform = "CoinbasePro", // Use String for now
            comment = None,
            externalId = Some(activity.externalId)
          )
        } else {
          Withdraw(
            user,
            activity.createdAt,
            Amount(activity.amount, account.currency),
            fee = None,
            platform = "CoinbasePro", // Use String for now
            comment = None,
            externalId = Some(activity.externalId)
          )
        }
      }
  }
}
