package it.softfork.shijianji

import java.time.ZonedDateTime
import java.util.UUID

import it.softfork.shijianji._
import org.scalatest.{FlatSpec, Matchers}

class TransactionSpec extends FlatSpec with Matchers {
  val user = User(id = UserId(UUID.randomUUID()), email = "user@test.com")
  val euro = Currency("EUR")
  val btc = Currency("BTC")
  val platform = "CoinbasePro"

  "Transaction.balance" should "get a balance for a currency" in {
    val transactions = Seq(
      Deposit(
        user= user,
        timestamp= ZonedDateTime.now(),
        amount= Amount(100 ,euro),
        fee= None,
        platform = platform,
        comment = None,
        externalId = None
      ),
      Trade(
        user = user,
        timestamp = ZonedDateTime.now(),
        soldAmount= Amount(50, euro),
        boughtAmount= Amount(1, btc),
        fee= None,
        platform= platform,
        comment= None,
        externalId= None
      )
    )
    Transaction.balance(euro, transactions) shouldBe Amount(50, euro)
    Transaction.balance(btc, transactions) shouldBe Amount(1, btc)
  }
}
