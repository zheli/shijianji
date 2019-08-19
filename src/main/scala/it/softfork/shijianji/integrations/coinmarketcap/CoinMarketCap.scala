package it.softfork.shijianji.integrations.coinmarketcap

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.typesafe.scalalogging.StrictLogging
import it.softfork.shijianji.CoinMarketCapConfig
import org.knowm.xchange.ExchangeFactory
import org.knowm.xchange.coinmarketcap.pro.v1.CmcExchange
import org.knowm.xchange.currency.CurrencyPair

import scala.concurrent.{ExecutionContext, Future}

class CoinMarketCap(config: CoinMarketCapConfig)(
  implicit system: ActorSystem,
  materializer: Materializer,
  ec: ExecutionContext
) extends StrictLogging {
  val client = {
    if (config.useSandbox) {
      logger.info("Use sandbox environment")
      val sandboxSpec = new CmcExchange().getSandboxExchangeSpecification
      sandboxSpec.setApiKey(config.sandboxKey)
      ExchangeFactory.INSTANCE.createExchange(sandboxSpec)
    } else {
      val spec = new CmcExchange().getDefaultExchangeSpecification
      spec.setApiKey(config.key)
      ExchangeFactory.INSTANCE.createExchange(spec)
    }
  }
  val marketDataService = client.getMarketDataService

  def ticker(currencyPair: CurrencyPair) = Future(marketDataService.getTicker(currencyPair))
}
