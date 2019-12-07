package it.softfork.shijianji

import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._

case class ShijianjiConfig(
  integrations : IntegrationConfig
)

object ShijianjiConfig {
  def load(): ShijianjiConfig = {
    ConfigFactory.load().as[ShijianjiConfig]("shijianji")
  }
}

case class IntegrationConfig (
  coinbasepro: CoinbaseproConfig,
  etherscan: EtherscanConfig
)

case class CoinbaseproConfig(
  pass: String,
  apiKey: String,
  apiSecret: String
)

case class EtherscanConfig(
  apikey: String
)
