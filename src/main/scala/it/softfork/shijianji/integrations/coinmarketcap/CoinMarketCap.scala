package it.softfork.shijianji.integrations.coinmarketcap

import akka.actor.ActorSystem
import akka.http.scaladsl.model.MediaTypes
import akka.http.scaladsl.model.headers.{Accept, RawHeader}
import akka.stream.Materializer
import com.typesafe.scalalogging.StrictLogging
import it.softfork.shijianji.CoinMarketCapConfig
import org.knowm.xchange.coinmarketcap.pro.v1.CmcExchange
import org.knowm.xchange.currency.CurrencyPair
import org.knowm.xchange.{ExchangeFactory, ExchangeSpecification}

import scala.concurrent.{ExecutionContext, Future}

class CoinMarketCap(config: CoinMarketCapConfig)(
  implicit system: ActorSystem,
  materializer: Materializer,
  ec: ExecutionContext
) extends StrictLogging {
  val client = if (config.useSandbox) {
    logger.info("Use sandbox environment")
    val sandboxSpec = new CmcExchange().getSandboxExchangeSpecification
    sandboxSpec.setApiKey(config.sandboxKey)
    ExchangeFactory.INSTANCE.createExchange(sandboxSpec)
  } else {
    val spec = new CmcExchange().getDefaultExchangeSpecification
    spec.setApiKey(config.key)
    ExchangeFactory.INSTANCE.createExchange(spec)
  }
  val marketDataService = client.getMarketDataService

  private def authHeaders = {
    Seq(
      Accept(MediaTypes.`application/json`),
      RawHeader("X-CMC_PRO_API_KEY", config.key)
    )
  }

  def ticker(currencyPair: CurrencyPair) = Future(marketDataService.getTicker(currencyPair))

//  private def get(uri: Uri): Future[HttpResponse] = {
//    logger.info(s"Sending request to $uri")
//
//    val request = HttpRequest(uri = uri)
//    val requestWithHeader = request.withHeaders(request.headers ++ authHeaders(uri, "GET", ""))
//    Http().singleRequest(requestWithHeader)
//  }
}
