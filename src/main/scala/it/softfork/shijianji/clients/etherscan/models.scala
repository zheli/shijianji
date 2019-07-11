package it.softfork.shijianji.clients.etherscan

import java.time.ZonedDateTime

import com.micronautics.web3j.Address
import it.softfork.debug4s.DebugMacro._
import it.softfork.shijianji._
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class EtherTransaction(
  blockNumber: Int,
  hash: String,
  value: BigDecimal,
  from: String,
  to: Option[String]
  //TOOD add timestamp
)

object EtherTransaction {
  implicit val etherTransactionReader: Reads[EtherTransaction] = (
    (JsPath \ "blockNumber").read[String].map(_.toInt) and
      (JsPath \ "hash").read[String] and
      // response value is ETH value * 10^18
      (JsPath \ "value").read[String].map(BigDecimal(_)/BigDecimal("1000000000000000000")) and
      (JsPath \ "from").read[String] and
      (JsPath \ "to").readNullable[String]
    )(EtherTransaction.apply _)

  def toTransaction(user: User, etherTransactions: Seq[EtherTransaction], walletAddress: Address): Seq[NonTradingTransaction] = {
    val lowerCaseAddress = walletAddress.value.toLowerCase()
    etherTransactions
      .map { t =>
        debug(t)
        t
      }
      .collect {
        case w: EtherTransaction if (w.from.toLowerCase == lowerCaseAddress) =>
          Withdraw(
            user = user,
            timestamp = ZonedDateTime.now, // TODO fix this
            amount = Amount(w.value, Currency("ETH")).negate,
            fee = None, // TODO fix this later
            platform =  "ethereum",
            comment = None,
            externalId = Some(w.hash)
          )
        case d: EtherTransaction if (d.to.map(_.toLowerCase) == Some(lowerCaseAddress)) =>
          Deposit(
            user = user,
            timestamp = ZonedDateTime.now, // TODO fix this
            amount = Amount(d.value, Currency("ETH")),
            fee = None, // TODO fix this later
            platform =  "ethereum",
            comment = None,
            externalId = Some(d.hash)
          )
      }
  }
}

case class EtherAccountTransactionsResponse(
  status: String,
  message: String,
  result: Seq[EtherTransaction]
)

object EtherAddressTransactionsResponse {
  implicit val reader: Reads[EtherAccountTransactionsResponse] = Json.reads[EtherAccountTransactionsResponse]
}
