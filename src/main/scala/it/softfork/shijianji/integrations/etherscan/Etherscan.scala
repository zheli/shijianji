package it.softfork.shijianji.integrations.etherscan

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.micronautics.web3j.Address
import com.typesafe.scalalogging.StrictLogging
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import it.softfork.shijianji._
import it.softfork.shijianji.users.User
import it.softfork.shijianji.utils.RichFutureResponse

import scala.concurrent.ExecutionContext

class Etherscan(config: EtherscanConfig)(
  implicit system: ActorSystem,
  materializer: Materializer,
  ec: ExecutionContext
) extends PlayJsonSupport
    with StrictLogging {
  private val baseUri = Uri("https://api.etherscan.io/api")
  // private def get(uri: Uri): Future[HttpResponse] = {
  //   logger.info(s"Sending request to $uri")

  //   val request = HttpRequest(uri = uri)
  //   val requestWithHeader = request.withHeaders(request.headers ++ authHeaders(uri, "GET", ""))
  //   Http().singleRequest(requestWithHeader)
  // }
  def normalTransactions(address: Address) = {
    import EtherAddressTransactionsResponse.reader
    logger.debug(s"Getting ether transactions for wallet $address")

    val parameters = Map(
      "module" -> "account",
      "action" -> "txlist",
      "address" -> address.value,
      "startblock" -> "0",
      "endblock" -> "99999999", // eth block generation time is ~10s, this will cover first 30 years of eth
      "sort" -> "asc",
      "apikey" -> config.apikey
    )
    val uri = baseUri.withQuery(
      Uri.Query(parameters)
    )
    val request = HttpRequest(uri = uri)
    val user = User.testUser
    Http()
      .singleRequest(request)
      .asSuccessful[EtherAccountTransactionsResponse]
      .map { response =>
        logger.debug(s"Fetched ${response.result.length} transactions")
        EtherTransaction.toTransaction(user, response.result, address)
      }
    // Http().singleRequest(request).flatMap { response =>
    //   if (response.status.isSuccess) {
    //     response.entity.toStrict(30.seconds).map(_.data.utf8String)
    //   } else {
    //     throw new RuntimeException(s"$response")
    //   }
    // }
  }
}
