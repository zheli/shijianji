package it.softfork.clients.coinbasepro

import java.time.ZonedDateTime
import java.util.UUID

import it.softfork.shijianji.clients.coinbasepro.CoinbasePro.fillReads
import it.softfork.shijianji.clients.coinbasepro.{Fill, Price, Size, TradeId}
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.Json

class ModelSpec extends FlatSpec with Matchers {
  "clients.coinbasepro" should "parse Fill entry in json " in {
    val json =
      """
         |[
         |    {
         |        "trade_id": 74,
         |        "product_id": "BTC-USD",
         |        "price": "10.00",
         |        "size": "0.01",
         |        "order_id": "d50ec984-77a8-460a-b958-66f114b0de9b",
         |        "created_at": "2014-11-07T22:19:28.578544Z",
         |        "liquidity": "T",
         |        "fee": "0.00025",
         |        "settled": true,
         |        "side": "buy"
         |    }
         |]
       """.stripMargin
    val expectedResult = Seq(
      Fill(
        tradeId = TradeId(74),
        price = Price(10.00),
        size = Size(0.01),
        createdAt = ZonedDateTime.now(),
        liquidity = "T",
        orderId = UUID.fromString("d50ec984-77a8-460a-b958-66f114b0de9b"),
        settled = true,
        side = "buy",
        fee = "0.00025"
      )
    )

    Json.parse(json).as[Seq[Fill]] shouldBe expectedResult
  }

}
