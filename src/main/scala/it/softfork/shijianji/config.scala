package it.softfork.shijianji

import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._

case class ShijianjiConfig(
  etherscan: EtherscanConfig
)

object ShijianjiConfig {
  def load(): ShijianjiConfig = {
    ConfigFactory.load().as[ShijianjiConfig]("shijianji")
  }
}

case class EtherscanConfig(
  apikey: String
)
