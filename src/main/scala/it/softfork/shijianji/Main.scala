package it.softfork.shijianji

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import com.typesafe.scalalogging.StrictLogging
import it.softfork.shijianji.clients.coinbasepro.CoinbasePro

import scala.util.{Failure, Success}

// TODO fetch transactions from coinbase
// NOTE re-use 3rd party client library as much as possible
object Main extends App with StrictLogging {
  logger.info("App started.")

  implicit val system = ActorSystem("FetchStuff")
  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(system))
  import system.dispatcher

  val coinbaseproSandboxPass = "INSERT PASSPHASE"
  val coinbaseproSandboxApiKey = "INSERT KEY"
  val coinbaseproSandboxApiSecret = "INSERT SECRET"
  val coinbase = CoinbasePro(coinbaseproSandboxApiKey, coinbaseproSandboxApiSecret, coinbaseproSandboxPass)
  val responseFuture = coinbase.fills
  responseFuture
    .onComplete {
      case Success(res) => {
        println(res)
        println("App finished.")
        system.terminate()
      }
      case Failure(err) => {
        logger.error(s"wrong!", err)
        println("App failed.")
        system.terminate()
      }
    }
}
