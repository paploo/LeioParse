package net.paploo.leioparse.formatter.formatters

import java.time.format.DateTimeFormatter

import com.github.tototoshi.csv.CSVWriter
import net.paploo.leioparse.data.core.{Book, BookReport, BookStatistics, Session}
import net.paploo.leioparse.formatter.CSVFormatter
import net.paploo.leioparse.util.extensions.Implicits._
import net.paploo.leioparse.util.extensions.LoggingExtensions.Logging

class V2CSVFormatter extends CSVFormatter[Unit] with Logging {

  override def writeReports(reports: Seq[BookReport])(implicit CSVWriter: CSVWriter): Unit = {
    writeHeaders
    reports.foreach(writeReport)
  }

  private[this] def writeHeaders(implicit csv: CSVWriter): Seq[String] = V2CSVFormatter.headers.tap(csv.writeRow)

  private[this] def writeReport(report: BookReport)(implicit csv: CSVWriter): Seq[String] = report.stats.map { reportStats =>
    report.sessions.flatMap(writeSession(report.book, reportStats))
  } recover {
    case th =>
      logger.error(s"Encountered error formatting for lines due to $th on report $report", th)
      Seq.empty
  } getOrElse {
    logger.warn(s"Line not logged due to an error for $report")
    Seq.empty
  }

  private[this] def writeSession(book: Book, stats: BookStatistics)(session: Session)(implicit csv: CSVWriter): Seq[String] = Seq(
    book.title.value.toString,
    book.externalId.map(_.value).getOrElse(""),
    book.averageWordDensity.value.round.toString, //Convert to Int
    book.length.value.toString,
    stats.progress.blocksRead.value.toString,
    book.words.value.toString,
    stats.progress.wordsRead.value.toString,
    stats.progress.completed.value.toString,
    stats.progress.cumulativeReadingTime.toHours.formatted("%.2f"),
    stats.progress.calendarDuration.toDays.formatted("%.3f"),
    stats.sessionReadingRates.blockRate.value.round.toString, //blocks per hour
    stats.sessionReadingRates.wordRate.value.round.toString, //words per hour
    stats.estimates.timeRemaining.toHours.formatted("%.2f"),
    stats.estimates.completionDate.value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),

    session.startDate.value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    session.sessionRelativeStart(stats.calendarDateStats.start).toDays.formatted("%.3f"),
    session.duration.toMinutes.formatted("%.1f"),
    session.blocks.value.toString,
    session.words(book.averageWordDensity).value.toString,

    "0.0",
    "0",
    "0"
  ).tap(csv.writeRow)

}

object V2CSVFormatter {

  val headers: Seq[String] = Seq(
    "Book_Title",
    "Book_ID",
    "Book_Avg_Word_Density",
    "Book_Total_Blocks",
    "Book_Blocks_Read",
    "Book_Total_Words",
    "Book_Total_Words_Read",
    "Book_Progress",
    "Book_Cumulative_Read_Time_Hours",
    "Book_Calendar_Read_Time_Days",
    "Book_Reading_Block_Rate",
    "Book_Reading_Word_Rate",
    "Book_ETR_Hours",
    "Book_ETA",

    "Session_Start",
    "Session_Relative_Start_Day",
    "Session_Duration_Minutes",
    "Session_Blocks_Read",
    "Session_Words_Read",

    "Session_Cumulative_Duration_Hours",
    "Session_Cumulative_Blocks",
    "Session_Cumulative_Words"
  )

}
