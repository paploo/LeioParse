package net.paploo.leioparse.formatter.formatters

import java.io.PrintWriter
import java.time.{Duration, LocalDateTime}

import io.circe.generic.auto._
import io.circe.syntax._
import net.paploo.leioparse.data.core.{Book, BookReport, BookStatistics, Session}
import net.paploo.leioparse.formatter.WriterFormatter
import net.paploo.leioparse.util.extensions.Implicits._

class JsonFormatter extends WriterFormatter[Unit] {

  override def writeReports(reports: Seq[BookReport])(implicit writer: PrintWriter): Unit = writer.println(reportJson(reports))

  def reportJson(reports: Seq[BookReport]) = reports.map(JsonFormatter.JsonBookReport.fromCanonical).asJson
}

object JsonFormatter {

  case class JsonBookReport(book: JsonBook,
                            sessions: Seq[JsonSession],
                            stats: Option[JsonBookStatistics])

  object JsonBookReport {

    def fromCanonical(report: BookReport): JsonBookReport = JsonBookReport(
      book = JsonBook.fromCanonical(report.book),
      sessions = report.sessions.map(JsonSession.fromCanonical(Option(report.book))),
      stats = report.stats.toOption.map(JsonBookStatistics.fromCanonical)
    )

  }

  case class JsonBook(title: String,
                      externalId: Option[String],
                      startLocation: Int,
                      endLocation: Int,
                      length: Int,
                      averageWordDensity: Double)

  object JsonBook {

    def fromCanonical(book: Book): JsonBook = JsonBook(
      title = book.title.value,
      externalId = book.externalId.map(_.value),
      startLocation = book.startLocation.value,
      endLocation = book.endLocation.value,
      length = book.length.value,
      averageWordDensity = book.averageWordDensity.value
    )

  }

  case class JsonSession(bookTitle: String,
                         startDate: LocalDateTime,
                         endDate: LocalDateTime,
                         duration: Duration,
                         startLocation: Int,
                         endLocation: Int,
                         blocks: Int,
                         blockRate: Double,
                         blockPace: Double,
                         words: Option[Int],
                         wordRate: Option[Double])

  object JsonSession {

    def fromCanonical(book: Option[Book])(session: Session): JsonSession = JsonSession(
      bookTitle = session.bookTitle.value,
      startDate = session.startDate.value,
      endDate = session.endDate.value,
      duration = session.duration.value,
      startLocation = session.startLocation.value,
      endLocation = session.endLocation.value,
      blocks = session.blocks.value,
      blockRate = session.blockRate.value,
      blockPace = session.blockPace.value,
      words = book.map(_.averageWordDensity).map(session.words).map(_.value),
      wordRate = book.map(_.averageWordDensity).map(session.wordRate).map(_.value)
    )

  }

  case class JsonBookStatistics(calendarDateStats: JsonBookStatistics.CalendarDateStats,
                                locationStats: JsonBookStatistics.LocationStats,
                                progress: JsonBookStatistics.Progress,
                                sessionReadingRates: JsonBookStatistics.SessionReadingRates,
                                bookReadingRates: JsonBookStatistics.BookReadingRates,
                                estimates: JsonBookStatistics.Estimates)

  object JsonBookStatistics {

    case class CalendarDateStats(start: LocalDateTime,
                                 last: LocalDateTime)

    case class LocationStats(start: Int,
                             last: Int)

    case class Progress(completed: Double,
                        locationsRead: Int,
                        wordsRead: Int,
                        cumulativeReadingTime: Duration,
                        calendarDuration: Duration)

    case class SessionReadingRates(blockRate: Double,
                                   blockPace: Double,
                                   wordRate: Double)

    case class BookReadingRates(blockDailyRate: Double)

    case class Estimates(timeRemaining: Duration,
                         completionDate: LocalDateTime)

    def fromCanonical(stats: BookStatistics): JsonBookStatistics = JsonBookStatistics(
      calendarDateStats = stats.calendarDateStats.thru(s => CalendarDateStats(s.start.value, s.last.value)),
      locationStats = stats.locationStats.thru(s => LocationStats(s.start.value, s.last.value)),
      progress = stats.progress.thru(s => Progress(s.completed.value, s.locationsRead.value, s.wordsRead.value, s.cumulativeReadingTime.value, s.calendarDuration.value)),
      sessionReadingRates = stats.sessionReadingRates.thru(s => SessionReadingRates(s.blockRate.value, s.blockPace.value, s.wordRate.value)),
      bookReadingRates = stats.bookReadingRates.thru(s => BookReadingRates(s.blockDailyRate.value)),
      estimates = stats.estimates.thru(s => Estimates(s.timeRemaining.value, s.completionDate.value))
    )

  }

}