package net.paploo.leioparse.data.core

import cats.Show
import net.paploo.leioparse.data.core.BookStatistics.SessionCumulativeStatistics
import net.paploo.leioparse.util.quantities._

import scala.util.{Failure, Try}

/**
  * Object encapsulating aggregate statistics calculated from a book and its reading sessions.
  *
  * The values are subdivided into mildly logical groupings.
  */
case class BookStatistics(calendarDateStats: BookStatistics.CalendarDateStats,
                          locationStats: BookStatistics.LocationStats,
                          progress: BookStatistics.Progress,
                          sessionReadingRates: BookStatistics.SessionReadingRates,
                          bookReadingRates: BookStatistics.BookReadingRates,
                          estimates: BookStatistics.Estimates,
                          cumulativeSessionStatistics: Seq[SessionCumulativeStatistics]) {
}

object BookStatistics {

  def from(book: Book, sessions: Seq[Session]): Try[BookStatistics] =
    if (sessions.nonEmpty) fromNonEmptySessions(book, sessions).recoverWith { case th => Failure(StatisticsComputationException(s"Encountered error $th while computing statistics for $book with sessions $sessions", th))}
    else Failure(StatisticsComputationException(s"Could not compute statistics for book with no sessions: $book", cause = null))

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

  case class SessionCumulativeStatistics(blocks: Blocks,
                                         words: Words,
                                         completed: Ratio,
                                         duration: TimeSpan,
                                         calendarDuration: TimeSpan)

  object SessionCumulativeStatistics {

    val empty: SessionCumulativeStatistics = apply(Blocks.Zero,
                                                   Words.Zero,
                                                   Ratio.Zero,
                                                   TimeSpan.Zero,
                                                   TimeSpan.Zero)

  }

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
      val completed = if (book.length.isZero) Ratio.Zero else blocksRead / book.length
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
      val calendarDaysRemaining: TimeSpan = progress.calendarDuration * scaleFactor
      Estimates(
        timeRemaining = progress.cumulativeReadingTime * scaleFactor,
        calendarDaysRemaining = calendarDaysRemaining,
        completionDate = calendarDateStats.last + calendarDaysRemaining
      )
    }

    val emptyMemo = (SessionCumulativeStatistics.empty, Vector.empty[SessionCumulativeStatistics])
    val cumulativeSessionStatistics: Seq[SessionCumulativeStatistics] = sessions.foldLeft(emptyMemo){
      case ((acc, stats), session) =>
        val blocksRead = acc.blocks + session.blocks
        val completed = if (book.length.isZero) Ratio.Zero else blocksRead / book.length
        val nextAcc = SessionCumulativeStatistics(blocks = blocksRead,
                                                  words = acc.words + session.words(book.averageWordDensity),
                                                  completed = completed,
                                                  duration = acc.duration + session.duration,
                                                  calendarDuration = session.endDate - calendarDateStats.start)
        (nextAcc, stats :+ nextAcc)
    }._2

    apply(calendarDateStats,
          locationStats,
          progress,
          sessionReadingRates,
          bookReadingRates,
          estimates,
          cumulativeSessionStatistics)
  }

}
