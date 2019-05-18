package it.softfork.shijianji

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import com.typesafe.scalalogging.StrictLogging
import it.softfork.shijianji.clients.coinbasepro
import it.softfork.shijianji.clients.coinbasepro.{CoinbasePro, Fill, ProductId}

import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

// NOTE re-use 3rd party client library as much as possible
object Main extends App with StrictLogging {
  logger.info("App started.")

  implicit val system: ActorSystem = ActorSystem("Shijianji")
  implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(system))
  import system.dispatcher

  val coinbaseproSandboxPass = "INSERT PASSPHASE"
  val coinbaseproSandboxApiKey = "INSERT KEY"
  val coinbaseproSandboxApiSecret = "INSERT SECRET"

  val user = User(id = UserId(UUID.randomUUID()), email = "test@ha.com")

  val coinbaseSandbox = CoinbasePro(coinbaseproSandboxApiKey, coinbaseproSandboxApiSecret, coinbaseproSandboxPass, coinbasepro.sandboxBaseUri)

  val resultFuture = for {
    time <- coinbaseSandbox.time
    accounts <- coinbaseSandbox.accounts
    fills <- coinbaseSandbox.allFills
  } yield {
    logger.debug(s"Server time: $time")

    logger.debug(s"Got accounts: $accounts")
    accounts.foreach(println(_))

    logger.debug(s"Found ${fills.length} filled orders")
    fills.foreach { fill =>
      val transaction: Trade = Fill.toTrade(user, fill)
      println(fill)
      println(transaction)
      transaction.fee.foreach(f => logger.debug(s"Fee: $f"))
      logger.debug(s"Buying price: ${transaction.buyingPrice} ${transaction.soldAmount.currency}")
    }
  }

  resultFuture
    .map { _ =>
      logger.info("App finished.")
      system.terminate()
    }
    .recover{
      case NonFatal(ex) =>
        logger.error(s"wrong!", ex)
        println("App failed.")
        system.terminate()
    }
}
