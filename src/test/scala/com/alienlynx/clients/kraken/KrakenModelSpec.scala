package com.alienlynx.clients.kraken

import java.time.{Instant, ZoneId, ZonedDateTime}

import com.alienlynx.clients.kraken.Kraken.{OHLC, OHLCResponse, OHLCs, ResponseId, TradePair}
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.{JsObject, JsValue, Json}

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

    val jsonMultiple =
      """
        |[
        | [1530344400,"5473.9","5473.9","5465.1","5472.7","5467.7","2.07331647",13],
        | [1530344460,"5472.7","5472.7","5471.3","5471.3","5471.3","0.16950000",1]
        |]
      """.stripMargin
    Json.parse(jsonMultiple).as[Seq[OHLC]] shouldBe Seq(
      OHLC(ZonedDateTime.of(2018,6,30,7,40,0,0,ZoneId.of("UTC")), 5473.9,5473.9,5465.1,5472.7,5467.7,2.07331647,13),
      OHLC(ZonedDateTime.of(2018,6,30,7,41,0,0,ZoneId.of("UTC")), 5472.7,5472.7,5471.3,5471.3,5471.3,0.16950000,1)
    )
  }

  it should "parse OHLC response" in {
    val json =
      """
        |{
        |  "error": [],
        |  "result": {
        |    "XXBTZEUR":[
        |      [1530344400,"5473.9","5473.9","5465.1","5472.7","5467.7","2.07331647",13],
        |      [1530344460,"5472.7","5472.7","5471.3","5471.3","5471.3","0.16950000",1]
        |    ],
        |    "last": 1530387480
        |  }
        |}
      """.stripMargin
    Json.parse(json).as[OHLCResponse] shouldBe OHLCResponse(
      Seq.empty[Kraken.Error],
      OHLCs(
        TradePair("XXBTZEUR"),
        Seq(
          OHLC(ZonedDateTime.of(2018,6,30,7,40,0,0,ZoneId.of("UTC")), 5473.9,5473.9,5465.1,5472.7,5467.7,2.07331647,13),
          OHLC(ZonedDateTime.of(2018,6,30,7,41,0,0,ZoneId.of("UTC")), 5472.7,5472.7,5471.3,5471.3,5471.3,0.16950000,1)
        ),
        ResponseId(1530387480)
      )
    )
  }

}
