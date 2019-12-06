package it.softfork.shijianji

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import com.micronautics.web3j.Address
import com.typesafe.scalalogging.StrictLogging
import it.softfork.debug4s.DebugMacro._
import it.softfork.shijianji.clients.coinbasepro
import it.softfork.shijianji.clients.coinbasepro._
import it.softfork.shijianji.clients.etherscan._
import it.softfork.shijianji.models._
import it.softfork.shijianji.users.UserPostgresStorage
import it.softfork.shijianji.utils.Csv
import slick.basic.DatabaseConfig
import slick.jdbc
//import slick.jdbc.H2Profile.api._
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.{JdbcProfile, PostgresProfile}
import slick.lifted.TableQuery

import scala.concurrent._
import scala.concurrent.duration._
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

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
        val db = Database.forConfig("shijianji.database.postgres2")
        val userStorage = new UserPostgresStorage("shijianji.database.postgres2")
//        val users = TableQuery[Users]
//        val db = Database.forConfig("shijianji.database.h2mem1")
//        val db2: jdbc.PostgresProfile.backend.Database = Database.forConfig("shijianji.database.postgres2")
        try {
          Await.result(userStorage.setup, Duration.Inf)
          Await.result(userStorage.db.run(
            DBIO.seq(
              userStorage.users += User("John Doe", uuid = UUID.randomUUID()),
              userStorage.users += User("Fred Smith", uuid = UUID.randomUUID()),
              // print the users (select * from USERS)
              userStorage.users.result.map(println)
            )
          ), Duration.Inf)
        } finally userStorage.db.close()
        //        val storage = new Storage(databaseConfig)
//        val databaseConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig[JdbcProfile]("shijianji.database")
//        val userRepo = new UserRepository(databaseConfig)
//        val resultFuture= userRepo.list()
//          .map(debug(_))
//          .recover {
//            case NonFatal(ex) =>
//              logger.error("Failed", ex)
//              system.terminate()
//          }
//        Await.ready(resultFuture, 1.hour)
        sys.exit()

      case List("test-run-etherscan-client") =>
        val etherscan = new Etherscan(config.etherscan)
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
