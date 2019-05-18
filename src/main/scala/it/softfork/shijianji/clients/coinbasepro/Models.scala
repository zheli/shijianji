package it.softfork.shijianji.clients.coinbasepro

import java.time.ZonedDateTime
import java.util.UUID

import it.softfork.shijianji._
import play.api.libs.json.JsonConfiguration.Aux
import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json.{Format, Json, JsonConfiguration, Reads}
import tech.minna.playjson.macros.{json, jsonFlat}

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
  id: AccountId,
  currency: Currency,
  balance: BigDecimal,
  available: BigDecimal,
  hold: BigDecimal,
  profileId: UUID
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
)

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
            Amount(activity.amount, Currency("EUR")), // fix currency
            fee = None,
            platform = "CoinbasePro", // Use String for now
            comment = None,
            externalId = Some(activity.id.toString)
          )
        } else {
          Withdraw(
            user,
            activity.createdAt,
            Amount(activity.amount, Currency("EUR")), // fix currency
            fee = None,
            platform = "CoinbasePro", // Use String for now
            comment = None,
            externalId = Some(activity.id.toString)
          )
        }
      }
  }
}
