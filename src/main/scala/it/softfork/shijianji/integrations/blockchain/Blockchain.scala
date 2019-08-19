package it.softfork.shijianji.integrations.blockchain

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.typesafe.scalalogging.StrictLogging
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import it.softfork.shijianji.models.BitcoinAddress
import org.knowm.xchange.ExchangeFactory
import org.knowm.xchange.blockchain.{BlockchainExchange, dto, Blockchain => XChangeBlockchain}
import si.mazi.rescu.RestProxyFactory

import scala.concurrent.{ExecutionContext, Future}

class Blockchain()(
  implicit system: ActorSystem,
  materializer: Materializer,
  ec: ExecutionContext
) extends PlayJsonSupport
    with StrictLogging {
  private val spec = new BlockchainExchange().getDefaultExchangeSpecification
  private val exchange = ExchangeFactory.INSTANCE.createExchange(spec)
  private val blockchain: XChangeBlockchain =
    RestProxyFactory.createProxy(classOf[XChangeBlockchain], exchange.getExchangeSpecification.getSslUri)

  def address(address: BitcoinAddress): Future[dto.BitcoinAddress] = Future(blockchain.getBitcoinAddress(address.value))
}
