package it.softfork.shijianji.clients.coinbasepro

import java.nio.charset.StandardCharsets
import java.time.ZonedDateTime
import java.util.{Base64, UUID}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Accept, RawHeader}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import it.softfork.shijianji.utils.RichUri
import com.typesafe.scalalogging.StrictLogging
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import it.softfork.shijianji._
import it.softfork.shijianji.clients.coinbasepro
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json._
import tech.minna.playjson.macros.{json, jsonFlat}

import scala.async.Async.{async, await}
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.control.NonFatal

@jsonFlat case class TradeId(value: Int) extends AnyVal
@jsonFlat case class Product(value: String) extends AnyVal
@jsonFlat case class OrderId(value: UUID) extends AnyVal
case class Price(value: BigDecimal) extends AnyVal
case class Size(value: BigDecimal) extends AnyVal

case class Fill(
  tradeId: TradeId,
  productId: String,
  price: Price,
  size: Size,
  orderId: UUID,
  liquidity: String, // Use string for now
  createdAt: ZonedDateTime,
  fee: Option[BigDecimal],
  settled: Boolean,
  side: String // Use string for now
) {
  val boughtCurrency: String = productId.split("-").head
  val soldCurrency: String = productId.split("-").tail.head
  val soldAmount = Amount(value = price.value * size.value, currency = Currency(soldCurrency))
  val boughtAmount = Amount(value = size.value, currency = Currency(boughtCurrency))
}

case class CoinbaseProduct(
  id: String,
  baseCurrency: Currency,
  quoteCurrency: Currency,
  baseMinSize: String,
  baseMaxSize: String,
  quoteIncrement: String
)


object Fill {

  def toTransaction(user: User, fill: Fill): Trade = {
    val fee = fill.fee.map(Amount(_, fill.soldAmount.currency))

    Trade(
      user = user,
      timestamp = fill.createdAt,
      soldAmount = fill.soldAmount,
      boughtAmount = fill.boughtAmount,
      fee = fee,
      platform = "CoinbasePro", // Use String for now
      externalId = fill.tradeId.value.toString,
      extraJsonData = None
    )
  }
}

@json case class ErrorResponse(
  message: String
)

object CoinbasePro {

  def apply(
    apiKey: String,
    apiSecret: String,
    passphrase: String,
    baseUrl: Uri = coinbasepro.productionBaseUri
  )(implicit system: ActorSystem, materializer: Materializer, ec: ExecutionContext): CoinbasePro = {
    new CoinbasePro(apiKey, apiSecret, passphrase, baseUrl)
  }

  implicit val config = JsonConfiguration(SnakeCase)
  implicit val priceReads: Reads[Price] = Reads[Price] { json =>
    json.validate[String].map(s => Price(BigDecimal(s)))
  }
  implicit val sizeReads: Reads[Size] = Reads[Size] { json =>
    json.validate[String].map(s => Size(BigDecimal(s)))
  }
  implicit val fillReads: Reads[Fill] = Json.reads[Fill]
  implicit val coinbaseProductReads: Reads[CoinbaseProduct] = Json.reads[CoinbaseProduct]
}

/**
  *
  * @param apiKey
  * @param apiSecret
  * @param passphrase
  * @param baseUrl the base url for API, can be production or sandbox
  */
class CoinbasePro(
  apiKey: String,
  apiSecret: String,
  passphrase: String,
  baseUrl: Uri
)(
  implicit system: ActorSystem,
  materializer: Materializer,
  ec: ExecutionContext
) extends PlayJsonSupport
    with StrictLogging {
  import CoinbasePro._

  // Copied some code from
  // https://github.com/jar-o/gdax-scala/blob/master/src/main/scala/GDAX/api.scala#CoinbaseAuth
  private def authHeaders(uri: Uri, method: String, body: String) = {
    val timestamp: Long = System.currentTimeMillis / 1000

    def signature = {
      val requestPathWithQuery: String = uri.path.toString + "?" + uri.query()
      val message = timestamp + method.toUpperCase + requestPathWithQuery + body
      val hmacKey: Array[Byte] = Base64.getDecoder.decode(apiSecret)
      val secret = new SecretKeySpec(hmacKey, "HmacSHA256")
      val hmac = Mac.getInstance("HmacSHA256")
      hmac.init(secret)
      Base64.getEncoder.encodeToString(hmac.doFinal(message.getBytes))
    }

    Seq(
      Accept(MediaTypes.`application/json`),
      RawHeader("CB-ACCESS-KEY", apiKey),
      RawHeader("CB-ACCESS-SIGN", signature),
      RawHeader("CB-ACCESS-TIMESTAMP", timestamp.toString),
      RawHeader("CB-ACCESS-PASSPHRASE", passphrase),
    )
  }

  def products: Future[Seq[CoinbaseProduct]] = {
    val uri: Uri = baseUrl / "products"
    val request = HttpRequest(uri = uri)
    Http()
      .singleRequest(request)
      .flatMap { response =>
        if (response.status.isSuccess()) {
//          response.entity.toStrict(300.millis).map(_.data).map(x => println(x.utf8String))
          Unmarshal(response.entity).to[Seq[CoinbaseProduct]].map { products =>
            logger.debug(s"Fetched ${products.length} products")
            products
          }.recover {
            case ex => throw new RuntimeException("Error", ex)
          }
        } else {
          Unmarshal(response.entity).to[ErrorResponse].map(x => logger.error(x.message))
          throw new RuntimeException(s"$response")
        }
      }
  }

  def fills(productId: String): Future[Seq[Fill]] = {
    val baseUri: Uri = baseUrl / "fills"

    def fetch(oldestTrade: Option[TradeId], totalFills: Seq[Fill]): Future[Seq[Fill]] = {
      val uri = oldestTrade match {
        case Some(id) => baseUri ? s"product_id=$productId&after=${id.value}"
        case None => baseUri ? s"product_id=$productId"
      }
      logger.debug(s"Sending request to $uri")
      val request = HttpRequest(uri = uri)
      val requestWithHeader = request.withHeaders(request.headers ++ authHeaders(uri, "GET", ""))

      val futureResult = Http()
        .singleRequest(requestWithHeader)
        .flatMap { response =>
          if (response.status.isSuccess()) {
            Unmarshal(response.entity).to[Seq[Fill]].map { fills: Seq[Fill] =>
              logger.debug(s"Fetched ${fills.length} fills")
              fills
            }
          } else {
            Unmarshal(response.entity).to[ErrorResponse].map(x => logger.error(x.message))
            throw new RuntimeException(s"$response")
          }
        }

      futureResult.flatMap { currentFills: Seq[Fill] =>
        // The result will be empty if previous page was the last page
        if (currentFills.isEmpty) {
          Future(totalFills)
        } else {
          val oldestCurrentFill = currentFills.reverse.head
          fetch(Some(oldestCurrentFill.tradeId), currentFills ++ totalFills)
        }
      }
    }

    import it.softfork.shijianji.utils.zonedDateTimeOrdering

    fetch(oldestTrade = None, totalFills = Seq.empty[Fill]).map(_.sortBy(_.createdAt))
  }

  def trades = {
    val allProducts = products
  }
}
