package it.softfork.shijianji.clients.coinbasepro

import java.util.UUID

import it.softfork.shijianji.Currency
import play.api.libs.json.JsonConfiguration.Aux
import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json.{Format, Json, JsonConfiguration}

case class Account(
  id: UUID,
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
