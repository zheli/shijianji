package it.softfork.shijianji.clients.etherscan

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class EtherTransaction(
  blockNumber: Int,
  value: BigDecimal,
  from: String,
  to: Option[String]
  //TOOD add timestamp
)

object EtherTransaction {
  implicit val etherTransactionReader: Reads[EtherTransaction] = (
    (JsPath \ "blockNumber").read[String].map(_.toInt) and
      // response value is ETH value * 10^18
      (JsPath \ "value").read[String].map(BigDecimal(_)/BigDecimal("1000000000000000000")) and
      (JsPath \ "from").read[String] and
      (JsPath \ "to").readNullable[String]
    )(EtherTransaction.apply _)
}

case class EtherAccountTransactionsResponse(
  status: String,
  message: String,
  result: Seq[EtherTransaction]
)

object EtherAddressTransactionsResponse {
  implicit val reader: Reads[EtherAccountTransactionsResponse] = Json.reads[EtherAccountTransactionsResponse]
}
