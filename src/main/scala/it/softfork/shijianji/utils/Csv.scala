package it.softfork.shijianji.utils

import java.io.File
import java.time.format.DateTimeFormatter

import com.github.tototoshi.csv.CSVWriter
import it.softfork.shijianji._

object Csv {

  def toCsvFile(filename: String, transactions: Seq[Transaction]): Unit = {
    val f = new File(filename)
    val writer = CSVWriter.open(f)
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    writer.writeRow(List("Timestamp", "Type", "Sell", "Sell Currency", "Buy", "Buy Currency"))

    transactions.foreach {
      case t: Trade =>
        // Sold amount should be negative
        writer.writeRow(
          List(
            t.timestamp.toLocalDateTime.format(formatter),
            t.productPrefix,
            t.soldAmount.value,
            t.soldAmount.currency.value,
            t.boughtAmount.value,
            t.boughtAmount.currency.value
          )
        )

      case d: Deposit =>
        writer.writeRow(
          List(
            d.timestamp.toLocalDateTime.format(formatter),
            d.productPrefix,
            d.amount.value,
            d.amount.currency.value,
            "",
            ""
          )
        )

      case w: Withdraw =>
        writer.writeRow(
          List(
            w.timestamp.toLocalDateTime.format(formatter),
            w.productPrefix,
            "",
            "",
            w.amount.value,
            w.amount.currency.value
          )
        )
    }
    writer.close()
  }
}
