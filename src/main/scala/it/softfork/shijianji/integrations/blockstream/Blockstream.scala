package it.softfork.shijianji.integrations.blockstream

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.stream.Materializer
import com.typesafe.scalalogging.StrictLogging
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import it.softfork.shijianji.CryptoCurrencyAddresses
import it.softfork.shijianji.models.{Asset, BitcoinAddress}
import it.softfork.shijianji.utils._

import scala.concurrent.ExecutionContext

class Blockstream(addressConfig:CryptoCurrencyAddresses)(
  implicit system: ActorSystem,
  materializer: Materializer,
  ec: ExecutionContext
) extends PlayJsonSupport
    with StrictLogging {
  private val baseUri = Uri("https://blockstream.info/api")

  def address(address: BitcoinAddress) = {
    import BlockstreamAddressResponse.formatter

    logger.debug(s"Getting address information for bitcoin wallet $address")
    val uri = baseUri / "address" / address.value
    val request = HttpRequest(uri = uri)
    Http()
      .singleRequest(request)
      .asSuccessful[BlockstreamAddressResponse]
      .map { response =>
        logger.debug(s"Fetched $response")
        response
      }
  }

  def btcAsset = {
    val btcAddresses = addressConfig.bitcoinAddresses
    FutureCollection.mapSequential(btcAddresses)(address).map { result =>
      val assets = result.map(addr => addr.toAsset)
      val balanceTotal = assets.map(_.balance).sum
      Asset("BTC", balanceTotal)
    }
  }
}
