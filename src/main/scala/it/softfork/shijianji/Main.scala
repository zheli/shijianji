package it.softfork.shijianji

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import com.typesafe.scalalogging.StrictLogging
import it.softfork.shijianji.clients.coinbasepro
import it.softfork.shijianji.clients.coinbasepro.CoinbasePro

import scala.util.{Failure, Success}

// TODO fetch transactions from coinbase
// NOTE re-use 3rd party client library as much as possible
object Main extends App with StrictLogging {
  logger.info("App started.")

  implicit val system = ActorSystem("Shijianji")
  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(system))
  import system.dispatcher

  val coinbaseproSandboxPass = "INSERT PASSPHASE"
  val coinbaseproSandboxApiKey = "INSERT KEY"
  val coinbaseproSandboxApiSecret = "INSERT SECRET"
  val coinbase = CoinbasePro(coinbaseproSandboxApiKey, coinbaseproSandboxApiSecret, coinbaseproSandboxPass, coinbasepro.sandboxBaseUri)
  val responseFuture = coinbase.fills("BTC-EUR")
  responseFuture
    .onComplete {
      case Success(res) => {
        println(res)
        logger.debug(s"Found ${res.length} filled orders")
        logger.info("App finished.")
        system.terminate()
      }
      case Failure(err) => {
        logger.error(s"wrong!", err)
        println("App failed.")
        system.terminate()
      }
    }
}
