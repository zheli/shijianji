package com.alienlynx.clients.kraken

import java.time.{Instant, ZoneId, ZonedDateTime}

import play.api.libs.json._
import tech.minna.playjson.macros.json


object Kraken {

  @json case class Error(value: String) extends AnyVal

  @json case class ResponseId(value: Int) extends AnyVal

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

  implicit val OHLCEntryReads: Reads[OHLC] = Reads[OHLC] { in =>
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

  case class OHLCResponse(
    error: Seq[Error],
    result: Seq[OHLC],
    last: ResponseId
  )

}
