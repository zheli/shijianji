package it.softfork.shijianji.clients.coinbasepro

import java.util.UUID

import it.softfork.shijianji.Currency
import play.api.libs.json.JsonConfiguration.Aux
import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json.{Format, Json, JsonConfiguration}
import tech.minna.playjson.macros.jsonFlat

@jsonFlat case class AccountId(value: UUID) extends AnyVal
@jsonFlat case class TradeId(value: Int) extends AnyVal
@jsonFlat case class ProductId(value: String) extends AnyVal
@jsonFlat case class OrderId(value: UUID) extends AnyVal

case class Account(
  id: AccountId,
  currency: Currency,
  balance: BigDecimal,
  available: BigDecimal,
  hold: BigDecimal,
  profileId:UUID
)

object Account {
  implicit val config: Aux[Json.MacroOptions] = JsonConfiguration(SnakeCase)
  implicit val formatter: Format[Account] = Json.format[Account]
}
