package it.softfork.clients.etherscan

import java.time.{ZoneId, ZonedDateTime}
import java.util.UUID

import com.micronautics.web3j.Address
import it.softfork.shijianji.{Amount, Currency, Withdraw}
import it.softfork.shijianji.clients.etherscan._
import it.softfork.shijianji.clients.etherscan.EtherAddressTransactionsResponse.reader
import it.softfork.shijianji.models.{User, UserId}
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.Json

class ModelSpec extends FlatSpec with Matchers {
  val user = User(uuid = UUID.randomUUID(), email = "user@test.com")
  val testJson =
    """
    |{
    |    "status": "1",
    |    "message": "OK",
    |    "result": [
    |        {
    |            "blockNumber": "65204",
    |            "timeStamp": "1439232889",
    |            "hash": "0x98beb27135aa0a25650557005ad962919d6a278c4b3dde7f4f6a3a1e65aa746c",
    |            "nonce": "0",
    |            "blockHash": "0x373d339e45a701447367d7b9c7cef84aab79c2b2714271b908cda0ab3ad0849b",
    |            "transactionIndex": "0",
    |            "from": "0x3fb1cd2cd96c6d5c0b5eb3322d807b34482481d4",
    |            "to": "0xde0b295669a9fd93d5f28d9ec85e40f4cb697bae",
    |            "value": "11901464239480000000000000",
    |            "gas": "122261",
    |            "gasPrice": "50000000000",
    |            "isError": "0",
    |            "txreceipt_status": "",
    |            "input": "0xf00d4b5d000000000000000000000000036c8cecce8d8bbf0831d840d7f29c9e3ddefa63000000000000000000000000c5a96db085dda36ffbe390f455315d30d6d3dc52",
    |            "contractAddress": "",
    |            "cumulativeGasUsed": "122207",
    |            "gasUsed": "122207",
    |            "confirmations": "7899333"
    |        },
    |        {
    |            "confirmations": "7899631",
    |            "gasUsed": "122207",
    |            "cumulativeGasUsed": "122207",
    |            "contractAddress": "",
    |            "input": "0xf00d4b5d00000000000000000000000005096a47749d8bfab0a90c1bb7a95115dbe4cea60000000000000000000000005ed8cee6b63b1c6afce3ad7c92f4fd7e1b8fad9f",
    |            "txreceipt_status": "",
    |            "isError": "0",
    |            "gasPrice": "50000000000",
    |            "gas": "122269",
    |            "value": "0",
    |            "to": "0xde0b295669a9fd93d5f28d9ec85e40f4cb697bae",
    |            "from": "0x3fb1cd2cd96c6d5c0b5eb3322d807b34482481d4",
    |            "transactionIndex": "0",
    |            "blockHash": "0x889d18b8791f43688d07e0b588e94de746a020d4337c61e5285cd97556a6416e",
    |            "nonce": "1",
    |            "hash": "0x621de9a006b56c425d21ee0e04ab25866fff4cf606dd5d03cf677c5eb2172161",
    |            "timeStamp": "1439235315",
    |            "blockNumber": "65342"
    |        }
    |    ]
    |}
    """.stripMargin

  "clients.etherscan" should "parse ethereum account transactions in json " in {
    val expectedResult = EtherAccountTransactionsResponse(
      status = "1",
      message = "OK",
      result = Seq(
        EtherTransaction(
          blockNumber = 65204,
          hash = "0x98beb27135aa0a25650557005ad962919d6a278c4b3dde7f4f6a3a1e65aa746c",
          timestamp = "1439232889",
          value = BigDecimal("11901464.23948"),
          from = "0x3fb1cd2cd96c6d5c0b5eb3322d807b34482481d4",
          to = Option("0xde0b295669a9fd93d5f28d9ec85e40f4cb697bae")
        ),
        EtherTransaction(
          blockNumber = 65342,
          hash = "0x621de9a006b56c425d21ee0e04ab25866fff4cf606dd5d03cf677c5eb2172161",
          timestamp = "1439235315",
          value = BigDecimal("0"),
          from = "0x3fb1cd2cd96c6d5c0b5eb3322d807b34482481d4",
          to = Option("0xde0b295669a9fd93d5f28d9ec85e40f4cb697bae")
        )
      )
    )

    Json.parse(testJson).as[EtherAccountTransactionsResponse] shouldBe expectedResult
  }

  "EtherTransaction" should "be able to convert to NonTradeTransaction" in {
    val transaction1 = EtherTransaction(
      blockNumber = 1,
      hash = "0xffff",
      timestamp = "1439232889",
      value = BigDecimal("1"),
      from = "0x00",
      to = Option("0x01")
    )
    val result = EtherTransaction.toTransaction(
      user = user,
      etherTransactions = Seq(transaction1),
      walletAddress = Address("0x00")
    )
    val expectedResult = Seq(
      Withdraw(
        user = user,
        timestamp = ZonedDateTime.of(2015, 8, 10, 18, 54,49, 0, ZoneId.of("UTC")),
        amount = Amount(-1, Currency("ETH")),
        fee = None,
        platform = "ethereum",
        comment = None,
        externalId = Some("0xffff")
      )
    )

    result shouldBe expectedResult
  }
}
