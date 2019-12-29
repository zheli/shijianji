package it.softfork.shijianji

import com.typesafe.config.ConfigFactory
import it.softfork.shijianji.models.BitcoinAddress
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._

case class ShijianjiConfig(
  integrations : IntegrationConfig,
  cryptocurrencyAddresses: CryptoCurrencyAddresses
)

object ShijianjiConfig {
  def load(): ShijianjiConfig = {
    ConfigFactory.load().as[ShijianjiConfig]("shijianji")
  }
}

case class IntegrationConfig (
  coinmarketcap: CoinMarketCapConfig,
  coinbasepro: CoinbaseproConfig,
  etherscan: EtherscanConfig
)

case class CoinMarketCapConfig(
  key: String,
  sandboxKey: String,
  useSandbox: Boolean
)

case class CoinbaseproConfig(
  pass: String,
  apiKey: String,
  apiSecret: String
)

case class EtherscanConfig(
  apikey: String
)

case class CryptoCurrencyAddresses(
  bitcoin: Seq[String]
) {
  def bitcoinAddresses = bitcoin.map(BitcoinAddress(_))
}
