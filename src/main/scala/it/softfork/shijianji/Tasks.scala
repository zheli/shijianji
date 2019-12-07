package it.softfork.shijianji

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.typesafe.scalalogging.StrictLogging
import it.softfork.shijianji.clients.coinbasepro
import it.softfork.shijianji.clients.coinbasepro.CoinbasePro

import scala.concurrent.ExecutionContext

object Tasks extends StrictLogging {

  def downloadTransactions(
    configs: IntegrationConfig
  )(implicit system: ActorSystem, materializer: Materializer, ec: ExecutionContext) = {
    logger.info("Run task - download transactions")

    val coinbase =
      CoinbasePro(configs.coinbasepro.apiKey, configs.coinbasepro.apiSecret, configs.coinbasepro.pass, coinbasepro.productionBaseUri)

    for {
      accounts <- coinbase.accounts
    } yield {
      println(accounts)
    }
  }

}
