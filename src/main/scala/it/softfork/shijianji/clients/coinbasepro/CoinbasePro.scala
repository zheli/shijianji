package it.softfork.shijianji.clients.coinbasepro

import java.nio.charset.StandardCharsets
import java.util.{Base64, UUID}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import it.softfork.shijianji.utils.RichUri
import com.typesafe.scalalogging.StrictLogging
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import it.softfork.shijianji.clients.coinbasepro
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json._
import tech.minna.playjson.macros.jsonFlat

import scala.concurrent.{ExecutionContext, Future}

@jsonFlat case class TradeId(value: Int) extends AnyVal
case class Price(value: Double) extends AnyVal
case class Size(value: Double) extends AnyVal
//@jsonFlat case class OrderId(value: UUID) extends AnyVal

case class Fill(
  tradeId: TradeId,
  price: Price,
  size: Size,
  orderId: UUID
)

object CoinbasePro {

  def apply(
    apiKey: String,
    apiSecret: String,
    passphrase: String
  )(implicit system: ActorSystem, materializer: Materializer, ec: ExecutionContext): CoinbasePro = {
    new CoinbasePro(apiKey, apiSecret, passphrase)
  }

  implicit val config = JsonConfiguration(SnakeCase)
  implicit val priceReads: Reads[Price] = Reads[Price] { json =>
    json.validate[String].map(s => Price(s.toDouble))
  }
  implicit val sizeReads: Reads[Size] = Reads[Size] { json =>
    json.validate[String].map(s => Size(s.toDouble))
  }
  implicit val fillReads: Reads[Fill] = Json.reads[Fill]
}

class CoinbasePro(
  apiKey: String,
  apiSecret: String,
  passphrase: String,
  baseUrl: Uri = coinbasepro.productionBaseUri
)(
  implicit system: ActorSystem,
  materializer: Materializer,
  ec: ExecutionContext
) extends PlayJsonSupport
    with StrictLogging {

  import CoinbasePro.fillReads

  // Copied some code from
  // https://github.com/jar-o/gdax-scala/blob/master/src/main/scala/GDAX/api.scala#CoinbaseAuth
  private def headers(url: Uri, method: String, body: String) = {
    val timestamp: Long = System.currentTimeMillis / 1000

    def signature = {
      val reqpath: String = url.path.toString
      val message = timestamp + method.toUpperCase + reqpath + body
      val hmacKey: Array[Byte] = Base64.getDecoder.decode(apiSecret)
      val secret = new SecretKeySpec(hmacKey, "HmacSHA256")
      val hmac = Mac.getInstance("HmacSHA256")
      hmac.init(secret)
      Base64.getEncoder.encodeToString(hmac.doFinal(message.getBytes))
    }

    Seq(
      RawHeader("CB-ACCESS-KEY", apiKey),
      RawHeader("CB-ACCESS-SIGN", signature),
      RawHeader("CB-ACCESS-TIMESTAMP", timestamp.toString),
      RawHeader("CB-ACCESS-PASSPHRASE", passphrase)
    )
  }

  def fills = {
    val uri: Uri = sandboxBaseUri / "fills"
    val futureResponse = Http().singleRequest(HttpRequest(uri = uri))
    futureResponse.flatMap { response: HttpResponse =>
      if (response.status.isSuccess()) {
        Unmarshal(response.entity).to[String]
//        response.entity.dataBytes.toString()
      } else {
        logger.error(s"$response")
        Future.failed(new RuntimeException(s"$response"))
      }
    }
  }
}
