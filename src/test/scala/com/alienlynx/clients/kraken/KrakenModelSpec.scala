package com.alienlynx.clients.kraken

import java.time.{Instant, ZoneId, ZonedDateTime}

import com.alienlynx.clients.kraken.Kraken.OHLC
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.Json

class KrakenModelSpec extends FlatSpec with Matchers {
  "clients.kraken" should "parse OHLC entry in json response" in {
    val json = """[1378339200,"97.0","97.0","96.7","96.7","96.9","2.75000000",5]"""
    val expectedResult = OHLC(
      time = ZonedDateTime.ofInstant(Instant.ofEpochSecond(1378339200), ZoneId.of("UTC")),
      open = 97.0,
      high = 97.0,
      low = 96.7,
      close = 96.7,
      vwap = 96.9,
      volume = 2.75000000,
      count = 5
    )

    Json.parse(json).as[OHLC] shouldBe expectedResult
  }

}
