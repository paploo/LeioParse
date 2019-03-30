package net.paploo.leioparse.data.core

import cats.Show
import net.paploo.leioparse.util.quantities._

import scala.util.{Failure, Try}

case class BookStatistics(calendarDateStats: BookStatistics.CalendarDateStats,
                          locationStats: BookStatistics.LocationStats,
                          progress: BookStatistics.Progress,
                          sessionReadingRates: BookStatistics.SessionReadingRates,
                          bookReadingRates: BookStatistics.BookReadingRates,
                          estimates: BookStatistics.Estimates) {
}

object BookStatistics {

  def from(book: Book, sessions: Seq[Session]): Try[BookStatistics] =
    if (sessions.nonEmpty) fromNonEmptySessions(book, sessions).recoverWith { case th => Failure(StatisticsComputationException(s"Encountered error $th while computing statistics for $book with sessions $sessions", th))}
    else Failure(StatisticsComputationException(s"Could not compute statiscs for book with no sessions: $book", cause = null))

  case class CalendarDateStats(start: DateTime,
                               last: DateTime)

  case class LocationStats(start: Location,
                           last: Location)

  case class Progress(completed: Ratio,
                      blocksRead: Blocks,
                      blocksRemaining: Blocks,
                      wordsRead: Words,
                      wordsRemaining: Words,
                      cumulativeReadingTime: TimeSpan,
                      calendarDuration: TimeSpan)

  case class SessionReadingRates(blockRate: BlockRate,
                                 blockPace: BlockPace,
                                 wordRate: WordRate)

  case class BookReadingRates(blockDailyRate: BlockDailyRate)

  case class Estimates(timeRemaining: TimeSpan,
                       calendarDaysRemaining: TimeSpan,
                       completionDate: DateTime)

  case class StatisticsComputationException(message: String, cause: Throwable) extends RuntimeException(message, cause)

  implicit val ShowBookStatistics: Show[BookStatistics] =
    s => (s.productIterator ++ List()).mkString(s"${s.productPrefix}(", ", ", ")")

  /**
    * Construct the statistics from a book and its sessions.
    *
    */
  private[this] def fromNonEmptySessions(book: Book, sessions: Seq[Session]): Try[BookStatistics] = Try {

    val calendarDateStats = CalendarDateStats(
      sessions.minBy(_.startDate).startDate,
      sessions.maxBy(_.endDate).endDate
    )

    val locationStats = LocationStats(
      sessions.minBy(_.startLocation).startLocation,
      sessions.maxBy(_.endLocation).endLocation
    )

    val progress = {
      val blocksRead = locationStats.start to locationStats.last
      val completed = if (book.length.isZero) Ratio.zero else blocksRead / book.length
      val blocksRemaining = book.length - blocksRead
      Progress(
        completed = completed,
        blocksRead = blocksRead,
        blocksRemaining = blocksRemaining,
        wordsRead = book.averageWordDensity * blocksRead,
        wordsRemaining = book.averageWordDensity * blocksRemaining,
        cumulativeReadingTime = sessions.foldLeft(TimeSpan.Zero)(_ + _.duration),
        calendarDuration = calendarDateStats.last - calendarDateStats.start
      )
    }

    val sessionReadingRates: SessionReadingRates = {
      val blockRate = if (progress.cumulativeReadingTime.isZero) BlockRate.Zero else progress.blocksRead / progress.cumulativeReadingTime
      SessionReadingRates(
        blockRate = blockRate,
        blockPace = if (blockRate.isZero) BlockPace.Zero else blockRate.inverse,
        wordRate = progress.wordsRead / progress.cumulativeReadingTime
      )
    }

    val bookReadingRates: BookReadingRates = BookReadingRates(
      blockDailyRate = BlockDailyRate.from(progress.blocksRead, progress.calendarDuration)
    )

    val estimates: Estimates = {
      val scaleFactor: Ratio = Ratio(progress.completed.inverse.value - 1.0)
      val calendardaysRemaining: TimeSpan = progress.calendarDuration * scaleFactor
      Estimates(
        progress.cumulativeReadingTime * scaleFactor,
        calendardaysRemaining,
        calendarDateStats.last + calendardaysRemaining
      )
    }

    apply(calendarDateStats,
          locationStats,
          progress,
          sessionReadingRates,
          bookReadingRates,
          estimates)
  }

}