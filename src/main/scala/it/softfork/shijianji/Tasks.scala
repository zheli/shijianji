package it.softfork.shijianji

import java.io.File

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.typesafe.scalalogging.StrictLogging
import it.softfork.debug4s.DebugMacro._
import it.softfork.shijianji.integrations.coinbasepro
import it.softfork.shijianji.integrations.coinbasepro.{Account, CoinbasePro}
import it.softfork.shijianji.integrations.coinmarketcap.CoinMarketCap
import it.softfork.shijianji.utils.FutureCollection
import com.github.tototoshi.csv.CSVWriter
import org.knowm.xchange
import org.knowm.xchange.currency.CurrencyPair
import org.knowm.xchange.dto.marketdata.Ticker

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

object Tasks extends StrictLogging {
  case class PortfolioAccount(
    asset: Currency,
    balance: BigDecimal,
    fiatAmount: Amount
  )

  def currentPortfolioToCSV(
    configs: IntegrationConfig
  )(implicit system: ActorSystem, materializer: Materializer, ec: ExecutionContext) = async {
    logger.info("Run task - download transactions")
    val targetFiat = Currency("USD")

    val coinbase =
      CoinbasePro(configs.coinbasepro.apiKey, configs.coinbasepro.apiSecret, configs.coinbasepro.pass, coinbasepro.productionBaseUri)
    val cmc = new CoinMarketCap(configs.coinmarketcap)

    def toPortfolioAccount(account: Account) = async {
      logger.debug(s"Current account: $account")
      val EURUSDExchangeRate = 1.12

      account.currency.value match {
        case "EUR" => PortfolioAccount(
          asset = account.currency,
          balance = account.balance,
          fiatAmount = Amount(account.balance * EURUSDExchangeRate, targetFiat)
        )

        case "USD" => PortfolioAccount(
          asset = account.currency,
          balance = account.balance,
          fiatAmount = Amount(account.balance, targetFiat)
        )

        case _ =>
          val xchangeCurrency = xchange.currency.Currency.getInstance(account.currency.value)
          val xchangeFiatCurrency = xchange.currency.Currency.getInstance(targetFiat.value)
          logger.debug(s"Xchange currency $xchangeCurrency")
          val xchangeCurrencyPair = new CurrencyPair(xchangeCurrency, xchangeFiatCurrency)
          logger.debug(s"Xchange currency pair $xchangeCurrencyPair")
          val ticker = await(cmc.ticker(xchangeCurrencyPair))
          PortfolioAccount(
            asset = account.currency,
            balance = account.balance,
            fiatAmount = Amount(account.balance * ticker.getLast, targetFiat)
          )

      }
    }

    val futurePortfolioAccounts = for {
      accounts: Seq[Account] <- coinbase.accounts
        portfolioAccounts <- {
          val updatedAccounts = accounts
            .filter(_.balance > 0)
          FutureCollection.mapSequential(updatedAccounts)(toPortfolioAccount)
        }
    } yield {
      portfolioAccounts
    }

    val f = new File("portfolio.csv")
    val writer = CSVWriter.open(f)
    writer.writeRow(List("Asset", s"${targetFiat.value} Value", "Balance" ))
    await {
      futurePortfolioAccounts.map(_.foreach { account =>
        logger.debug(s"Write $account to CSV file")
        writer.writeRow(
          List(
            account.asset.value,
            account.fiatAmount.value,
            account.balance
          )
        )
      }).recover {
        case NonFatal(ex) => logger.error("Something went wront", ex)
      }
    }
    writer.close
  }

}
