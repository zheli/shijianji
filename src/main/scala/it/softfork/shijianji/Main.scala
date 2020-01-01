package it.softfork.shijianji

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import com.micronautics.web3j.Address
import com.typesafe.scalalogging.StrictLogging
import it.softfork.debug4s.DebugMacro._
import it.softfork.shijianji.integrations.blockchain.Blockchain
import it.softfork.shijianji.integrations.blockstream.Blockstream
import it.softfork.shijianji.integrations.coinbasepro
import it.softfork.shijianji.integrations.coinbasepro._
import it.softfork.shijianji.integrations.etherscan._
import it.softfork.shijianji.models._
import it.softfork.shijianji.users.{User, UserPostgresStorage}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent._
import scala.concurrent.duration._
import scala.util.control.NonFatal

// re-use 3rd party client library as much as possible
object Main extends App with StrictLogging {

  logger.info("App started.")

  implicit val system: ActorSystem = ActorSystem("Shijianji")
  implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(system))
  import system.dispatcher

  val config = ShijianjiConfig.load()

  // Temporary user until we have a working user module
//  val user = User(id = UUID.randomUUID(), email = "test@ha.com")

  try {
    args.toList match {
      case Nil =>
        logger.info("Start API server")
        // TODO add api server start here
        sys.exit()

      case List("setup-db") =>
        val db = Database.forConfig("shijianji.database.postgres")
        try {
          Await.result(
            db.run(
              DBIO.seq(
                UserPostgresStorage.setup,
                UserPostgresStorage.users += User("John Doe", uuid = UUID.randomUUID()),
                UserPostgresStorage.users += User("Fred Smith", uuid = UUID.randomUUID()),
                // print the users (select * from USERS)
                UserPostgresStorage.users.result.map(println)
              )
            ), 1.hour)
        } finally db.close()
        sys.exit()

      case List("download-transaction-as-csv") =>
        Await.ready(Tasks.currentPortfolioToCSV(config.integrations), 1.hour)
        sys.exit()

      case List("test") =>
        val blockchain = new Blockstream(config.cryptocurrencyAddresses)
        val resultFuture = blockchain.btcAsset.recover {
          case NonFatal(ex) =>
            logger.error("Something bad happened", ex)
        }
        Await.ready(resultFuture.map(println), 1.hour)
        sys.exit()

      case List("test-run-etherscan-client") =>
        val etherscan = new Etherscan(config.integrations.etherscan)
        val resultFuture = {
          etherscan.normalTransactions(Address("0x6BeF00Ee10775d4DD51F1ee2443f17B6f298FC9D")).map(debug(_))
        }
        Await.ready(resultFuture, 1.hour)
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
