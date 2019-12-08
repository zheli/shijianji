package it.softfork.shijianji

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.typesafe.scalalogging.StrictLogging
import it.softfork.debug4s.DebugMacro._
import it.softfork.shijianji.integrations.coinbasepro
import it.softfork.shijianji.integrations.coinbasepro.CoinbasePro

import scala.concurrent.ExecutionContext

object Tasks extends StrictLogging {

  def currentPortfolio(
    configs: IntegrationConfig
  )(implicit system: ActorSystem, materializer: Materializer, ec: ExecutionContext) = {
    logger.info("Run task - download transactions")

    val coinbase =
      CoinbasePro(configs.coinbasepro.apiKey, configs.coinbasepro.apiSecret, configs.coinbasepro.pass, coinbasepro.productionBaseUri)

    for {
      accounts <- coinbase.accounts
    } yield {
      accounts.foreach(println(_))
    }
  }

}
