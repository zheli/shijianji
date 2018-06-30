package com.alienlynx.clients.kraken

import java.io
import java.time.{Instant, ZoneId, ZonedDateTime}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypeRange, HttpRequest, HttpResponse, MediaType}
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.server.{RejectionError, ValidationRejection}
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshal, Unmarshaller}
import akka.http.scaladsl.util.FastFuture
import akka.stream.Materializer
import akka.util.ByteString
import com.alienlynx.clients.kraken.Kraken._
import com.alienlynx.utils.{RichFutureResponse, RichUri}
import com.alienlynx.utils.Json.mapFormat
import play.api.libs.json._
import play.api.libs.functional.syntax._
import tech.minna.playjson.macros.{json, jsonFlat}
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import scala.util.control.NonFatal


object Kraken {

  @jsonFlat case class Error(value: String) extends AnyVal
  @jsonFlat case class ResponseId(value: Int) extends AnyVal
  @jsonFlat case class TradePair(name: String) extends AnyVal

  case class OHLC(
    time: ZonedDateTime,
    open: BigDecimal,
    high: BigDecimal,
    low: BigDecimal,
    close: BigDecimal,
    vwap: BigDecimal,
    volume: BigDecimal,
    count: Int
  )

  case class OHLCs(
    tradePair: TradePair,
    data: Seq[OHLC],
    id: ResponseId
  )

  case class OHLCResponse(
    error: Seq[Error],
    result: OHLCs,
  )

  implicit val OHLCReads: Reads[OHLC] = Reads[OHLC] { in =>
    def timeReads(js: JsValue): ZonedDateTime = {
      js.validate[Int].map { epoch =>
        ZonedDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneId.of("UTC"))
      }.getOrElse(throw new RuntimeException("Failed to parse time."))
    }

    def priceReads(js: JsValue): BigDecimal = {
      js.validate[BigDecimal].map { v: BigDecimal =>
        v
      }.getOrElse(throw new RuntimeException("Failed to parse price."))
    }

    def countReads(js: JsValue): Int = {
      js.validate[Int].map { v: Int =>
        v
      }.getOrElse(throw new RuntimeException("Failed to parse count."))
    }

    in.validate[Seq[JsValue]] match {
      case JsSuccess(list: Seq[JsValue], _) if list.length == 8 => {
        JsSuccess(
          OHLC(
            time = timeReads(list.head),
            open = priceReads(list(1)),
            high = priceReads(list(2)),
            low = priceReads(list(3)),
            close = priceReads(list(4)),
            vwap = priceReads(list(5)),
            volume = priceReads(list(6)),
            count = countReads(list(7))
          )
        )
      }

      case error: JsError => error

      case _ => JsError(s"Failed to parse data $in")
    }
  }

  implicit val ohlcsReads: Reads[OHLCs] = Reads[OHLCs] {
    case o: JsObject => {
      val id = o.values.last.as[ResponseId]
      for {
        pair <- o.keys.headOption.map(TradePair.apply)
        ohlcs <- o.values.headOption.map(_.as[Seq[OHLC]])
      } yield JsSuccess(OHLCs(pair, ohlcs, id))
    }.getOrElse(JsError("Cannot parse OHLC results"))

    case _ => JsError("Cannot parse OHLC results")
  }
  implicit val ohlcResponseReads: Reads[OHLCResponse] = Json.reads[OHLCResponse]

  @jsonFlat case class AssetName(value: String) extends AnyVal

  @json case class AssetInfo(
    aclass: String,
    altname: String,
    decimals: Int,
    display_decimals: Int
  )

  implicit val assetsFormatter: OFormat[Map[AssetName, AssetInfo]] = mapFormat[AssetName, AssetInfo](string => AssetName(string), _.value)

//  implicit val assetInfoResultMapFormatter: Reads[Map[AssetName, AssetInfo]] = Reads[Map[AssetName, AssetInfo]] { in =>
//    in.validate[Map[AssetName, AssetInfo]].map { mapItem =>
//      println(mapItem)
//      mapItem
//    }
//  }

//  implicit val krakenToResponseFormatter: Reads[KrakenResponse] = Reads[KrakenResponse] { in =>
//  }

  @json case class KrakenResponse(
    error: Seq[String],
    result: String
  )

}
