package it.softfork.shijianji.clients.coinbasepro

import java.time.ZonedDateTime
import java.util.{Base64, UUID}

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Accept, RawHeader}
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.typesafe.scalalogging.StrictLogging
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import it.softfork.shijianji._
import it.softfork.shijianji.clients.coinbasepro
import it.softfork.shijianji.utils.{RichFutureResponse, RichUri}
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json._
import tech.minna.playjson.macros.{json, jsonFlat}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

case class Fill(
  tradeId: TradeId,
  productId: ProductId,
  price: Price,
  size: Size,
  orderId: UUID,
  liquidity: String, // Use string for now
  createdAt: ZonedDateTime,
  fee: Option[BigDecimal],
  settled: Boolean,
  side: String // Use string for now
) {
  val boughtCurrency: String = productId.value.split("-").head
  val soldCurrency: String = productId.value.split("-").tail.head
  val soldAmount = Amount(value = price.value * size.value, currency = Currency(soldCurrency))
  val boughtAmount = Amount(value = size.value, currency = Currency(boughtCurrency))
}

case class CoinbaseProduct(
  id: ProductId,
  baseCurrency: Currency,
  quoteCurrency: Currency,
  baseMinSize: String,
  baseMaxSize: String,
  quoteIncrement: String
)

object Fill {

  def toTrade(user: User, fill: Fill): Trade = {
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

  val pageLimit = 100

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

    logger.trace(s"Current timestamp: $timestamp")

    Seq(
      Accept(MediaTypes.`application/json`),
      RawHeader("CB-ACCESS-KEY", apiKey),
      RawHeader("CB-ACCESS-SIGN", signature),
      RawHeader("CB-ACCESS-TIMESTAMP", timestamp.toString),
      RawHeader("CB-ACCESS-PASSPHRASE", passphrase)
    )
  }

  private def get(uri: Uri): Future[HttpResponse] = {
    logger.info(s"Sending request to $uri")

    val request = HttpRequest(uri = uri)
    val requestWithHeader = request.withHeaders(request.headers ++ authHeaders(uri, "GET", ""))
    Http().singleRequest(requestWithHeader)
  }

  def products: Future[Seq[CoinbaseProduct]] = {
    val uri: Uri = baseUrl / "products"

    get(uri)
      .asSuccessful[Seq[CoinbaseProduct]]
      .map { products =>
        logger.debug(s"Fetched ${products.length} products")
        products
      }
  }

  def fillsByProductId(productId: ProductId): Future[Seq[Fill]] = {
    val baseUri: Uri = baseUrl / "fills"

    def fetch(oldestTrade: Option[TradeId], totalFills: Seq[Fill]): Future[Seq[Fill]] = {
      val uri = oldestTrade match {
        case Some(id) => baseUri ? s"product_id=${productId.value}&after=${id.value}"
        case None => baseUri ? s"product_id=${productId.value}"
      }

      val futureResult = get(uri)
        .asSuccessful[Seq[Fill]]
        .map { fills =>
          logger.debug(s"Fetched ${fills.length} fills")
          fills
        }

      futureResult.flatMap { currentFills: Seq[Fill] =>
        // The result will be empty if previous page was the last page
        // Or when the number of results returned is less than page limit
        if (currentFills.isEmpty || currentFills.length < pageLimit) {
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

  def allFills: Future[Seq[Fill]] = {
    val source: Source[CoinbaseProduct, NotUsed] =
      Source.fromFuture(products).flatMapConcat(products => Source.fromIterator(() => products.toIterator))
    source
      .mapAsyncUnordered(10) { product =>
        fillsByProductId(product.id)
      }
      .mapConcat(_.to[collection.immutable.Seq])
      .runWith(Sink.seq)
      .collect {
        case fills: Seq[Fill] => fills
      }
  }

  def allTransfers: Future[Seq[AccountActivity]] = {
    val source: Source[Account, NotUsed] =
      Source.fromFuture(accounts).flatMapConcat(accounts => Source.fromIterator(() => accounts.toIterator))
    source
      .mapAsyncUnordered(10) { account: Account =>
        accountActivities(account.id)
      }
      .mapConcat { activities =>
        activities
          .filter(_.`type` != "transfer") // we only care about deposit and withdraw
          .to[collection.immutable.Seq]
      }
      .runWith(Sink.seq)
      .collect {
        case activities: Seq[AccountActivity] => activities
      }
  }

  def accounts: Future[Seq[Account]] = {
    import Account.formatter

    // Need to include "?" otherwise it will get invalid signature error from coinbase pro
    val uri: Uri = (baseUrl / "accounts") ? ""

    get(uri)
      .asSuccessful[Seq[Account]]
      .map { accounts =>
        logger.debug(s"Fetched ${accounts.length} accounts")
        accounts
      }
  }

  def accountActivities(accountId: AccountId): Future[Seq[AccountActivity]] = {
    import AccountActivity.formatter

    val uri: Uri = (baseUrl / "accounts" / accountId.value.toString / "ledger") ? ""

    // get(uri).flatMap { response =>
    //   if (response.status.isSuccess()) {
    //     response.entity.toStrict(300.millis).map(_.data).map(x => println(x.utf8String))
    //     Future(Seq.empty[AccountActivity])
    //   } else throw new RuntimeException(s"$response")
    // }
    get(uri)
      .asSuccessful[Seq[AccountActivity]]
      .map { accountActivities =>
        logger.debug(s"Fetched ${accountActivities.length} account activities")
        accountActivities
      }
  }

  def time: Future[String] = {
    val uri: Uri = baseUrl / "time"

    get(uri).flatMap { response =>
      if (response.status.isSuccess()) {
        response.entity.toStrict(1.second).map(_.data.utf8String)
      } else {
        throw new RuntimeException(s"$response")
      }
    }
  }
}
