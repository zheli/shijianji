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
import it.softfork.shijianji.{Trade, User}
import it.softfork.shijianji.clients.coinbasepro
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json._
import tech.minna.playjson.macros.{json, jsonFlat}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

@jsonFlat case class TradeId(value: Int) extends AnyVal
case class Price(value: Double) extends AnyVal
case class Size(value: Double) extends AnyVal
@jsonFlat case class OrderId(value: UUID) extends AnyVal

case class Fill(
  tradeId: TradeId,
  productId: String,
  price: Price,
  size: Size,
  orderId: UUID,
  liquidity: String, // Use string for now
  createdAt: ZonedDateTime,
  fee: String, // Use string for now
  settled: Boolean,
  side: String // Use string for now
)

object Fill {
  def toTransaction(user: User, fill: Fill): Trade = {
    val Array(sellProduct, buyProduct) = fill.productId.split("-")
    Trade(
      user = user,
      timestamp = fill.createdAt,
      sellProduct = sellProduct, // TODO change this
      sellAmount= 0.1,
      buyProduct= buyProduct, // Use String for now
      buyAmount= 0.1,
      platform= "CoinbasePro", // Use String for now
      extraJsonData= None
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
    json.validate[String].map(s => Price(s.toDouble))
  }
  implicit val sizeReads: Reads[Size] = Reads[Size] { json =>
    json.validate[String].map(s => Size(s.toFloat))
  }
  implicit val fillReads: Reads[Fill] = Json.reads[Fill]
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

  def fills(productId: String): Future[Seq[Fill]] = {
    import CoinbasePro.fillReads

    val uri: Uri = (baseUrl / "fills") ? s"product_id=$productId"
    logger.debug(s"Sending request to $uri")
    val request = HttpRequest(uri = uri)

    val futureResponse = Http().singleRequest(request.withHeaders(request.headers ++ authHeaders(uri, "GET", "")))
    futureResponse.flatMap { response: HttpResponse =>
      if (response.status.isSuccess()) {
//        response.entity.toStrict(300.millis).map(_.data).map(x => println(x.utf8String))
        Unmarshal(response.entity).to[Seq[Fill]]
      } else {
        Unmarshal(response.entity).to[ErrorResponse].map(x => logger.error(x.message))
        throw new RuntimeException(s"$response")
      }
    }
  }
}
