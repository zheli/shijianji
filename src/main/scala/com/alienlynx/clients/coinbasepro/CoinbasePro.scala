package com.alienlynx.clients.coinbasepro

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.alienlynx.utils.{RichFutureResponse, RichUri}
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json.JsonNaming.SnakeCase
import tech.minna.playjson.macros.{json, jsonFlat}
import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.concurrent.ExecutionContext

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
  implicit val config =  JsonConfiguration(SnakeCase)
  implicit val priceReads: Reads[Price] = Reads[Price] { json =>
    json.validate[String].map(s => Price(s.toDouble))
  }
  implicit val sizeReads: Reads[Size] = Reads[Size] { json =>
    json.validate[String].map(s => Size(s.toDouble))
  }
  implicit val fillReads: Reads[Fill] = Json.reads[Fill]
}

class CoinbasePro(implicit system: ActorSystem, materializer: Materializer, ec: ExecutionContext) extends PlayJsonSupport {
  import com.alienlynx.clients.coinbasepro.CoinbasePro.fillReads

  def fills = {
    val uri: Uri = baseUri / "fills?product_id=BTC-EUR"
    val futureResponse = Http().singleRequest(HttpRequest(uri = uri))
    futureResponse.asUnsafe[Seq[Fill]]
  }
}
