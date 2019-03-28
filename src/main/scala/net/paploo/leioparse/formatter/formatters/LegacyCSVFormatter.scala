package net.paploo.leioparse.formatter.formatters

import java.time.format.DateTimeFormatter

import com.github.tototoshi.csv.CSVWriter
import net.paploo.leioparse.data.core.{Book, BookReport, Session}
import net.paploo.leioparse.formatter.CSVFormatter
import net.paploo.leioparse.util.extensions.Implicits._

class LegacyCSVFormatter extends CSVFormatter[Unit] {

  override def writeReports(reports: Seq[BookReport])(implicit csv: CSVWriter): Unit = {
    writeHeaders
    reports.foreach(writeReport)
  }

  def writeHeaders(implicit csv: CSVWriter): Seq[String] = LegacyCSVFormatter.headers.tap(csv.writeRow)

  def writeReport(report: BookReport)(implicit csv: CSVWriter): Seq[String] = report.sessions.flatMap(writeSession(report.book))

  def writeSession(book: Book)(session: Session)(implicit csv: CSVWriter): Seq[String] = Seq(
    book.title.value.toString,
    book.externalId.map(_.value).getOrElse(""),
    book.averageWordDensity.value.round.toString, //Convert to Int
    session.startDate.value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    session.blocks.value.toString,
    session.duration.toSeconds.toString,
    session.blockRate.value.formatted("%.1f"),
    (session.blockPace.value * 60.0).formatted("%.1f"), //Convert from minutes/blocks to seconds/block.
    session.wordRate(book.averageWordDensity).value.formatted("%.1f")
  ).tap(csv.writeRow)

}

object LegacyCSVFormatter {

  val headers: Seq[String] = Seq(
    "Book",
    "Book_ID",
    "Book_Words_Per_Page",
    "Date",
    "Pages",
    "Duration_Sec",
    "Rate_PPH",
    "Pace_SPP",
    "Rate_WPM"
  )

}
