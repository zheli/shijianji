package it.softfork.shijianji.integrations.blockstream

import it.softfork.shijianji.models.{Asset, BitcoinAddress}
import play.api.libs.json.JsonConfiguration.Aux
import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json.{Format, Json, JsonConfiguration}

case class BlockstreamAddressResponse(
  address: BitcoinAddress,
  chainStats: ChainStats
) {
  def toAsset = Asset(
    "BTC",
    // Restore decimal in the value
    chainStats.fundedTxoSum / BigDecimal(100000000)
  )
}

object BlockstreamAddressResponse {
  import ChainStats.formatter
  implicit val config: Aux[Json.MacroOptions] = JsonConfiguration(SnakeCase)
  implicit val formatter: Format[BlockstreamAddressResponse] = Json.format[BlockstreamAddressResponse]
}

case class ChainStats(
  fundedTxoSum: BigDecimal
)

object ChainStats {
  implicit val config: Aux[Json.MacroOptions] = JsonConfiguration(SnakeCase)
  implicit val formatter: Format[ChainStats] = Json.format[ChainStats]
}
