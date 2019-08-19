package it.softfork.shijianji

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import com.micronautics.web3j.Address
import com.typesafe.scalalogging.StrictLogging
import it.softfork.debug4s.DebugMacro._
import it.softfork.shijianji.clients.etherscan._

import scala.concurrent._
import scala.concurrent.duration._


// re-use 3rd party client library as much as possible
object Main extends App with StrictLogging {

  logger.info("App started.")

  implicit val system: ActorSystem = ActorSystem("Shijianji")
  implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(system))
  import system.dispatcher
  val config = ShijianjiConfig.load()
  // Temporary user until we have a working user module
  val user = User(id = UserId(UUID.randomUUID()), email = "test@ha.com")

  try {
    args.toList match {
      case Nil =>
        logger.info("Start API server")
        // TODO add api server start here
        sys.exit()

      case List("test-run-etherscan-client") =>
        val etherscan = new Etherscan(config.etherscan)
        val resultFuture = {
          etherscan.normalTransactions(Address("0x6BeF00Ee10775d4DD51F1ee2443f17B6f298FC9D")).map(debug)
        }
        Await.ready(resultFuture, 1.hours)
        sys.exit()

      case args =>
        logger.info(s"Unrecognized command: $args")
        sys.exit()
    }
  } catch {
    case ex: Throwable =>
      logger.error("Error on application start", ex)
      // Wait a bit so that the logger have a chance to write to stdout
      Thread.sleep(100)
      throw ex
  }
}
