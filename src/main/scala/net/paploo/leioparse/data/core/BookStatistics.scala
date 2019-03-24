package net.paploo.leioparse.data.core

import net.paploo.leioparse.util.quantities._

case class BookStatistics(calendarDateStats: BookStatistics.CalendarDateStats,
                     locationStats: BookStatistics.LocationStats,
                     progress: BookStatistics.Progress,
                     sessionReadingRates: BookStatistics.SessionReadingRates,
                     bookReadingRates: BookStatistics.BookReadingRates,
                     estimates: BookStatistics.Estimates) {
}

object BookStatistics {

  def from(book: Book, sessions: Seq[Session]): Option[BookStatistics] =
    if (sessions.nonEmpty) Some(fromNonEmptySessions(book, sessions)) else None

  case class CalendarDateStats(start: DateTime,
                               last: DateTime)

  case class LocationStats(start: Location,
                           last: Location)

  case class Progress(completed: Ratio,
                      locationsRead: Blocks,
                      wordsRead: Words,
                      cumulativeReadingTime: TimeSpan,
                      calendarDuration: TimeSpan)

  case class SessionReadingRates(blockRate: BlockRate,
                                 blockPace: BlockPace,
                                 wordRate: WordRate)

  case class BookReadingRates(blockDailyRate: BlockDailyRate)

  case class Estimates(timeRemaining: TimeSpan,
                       completionDate: DateTime)

  private[this] def fromNonEmptySessions(book: Book, sessions: Seq[Session]): BookStatistics = {

    val calendarDateStats = CalendarDateStats(
      sessions.minBy(_.startDate).startDate,
      sessions.maxBy(_.endDate).endDate
    )

    val locationStats = LocationStats(
      sessions.minBy(_.startLocation).startLocation,
      sessions.maxBy(_.endLocation).endLocation
    )

    val progress = {
      val locationsRead = locationStats.start to locationStats.last
      Progress(
        completed = locationsRead / book.length,
        locationsRead = locationsRead,
        wordsRead = book.averageWordDensity * locationsRead,
        cumulativeReadingTime = sessions.foldLeft(TimeSpan.Zero)(_ + _.duration),
        calendarDuration = calendarDateStats.last - calendarDateStats.start
      )
    }

    val sessionReadingRates: SessionReadingRates = {
      val blockRate = progress.locationsRead / progress.cumulativeReadingTime
      SessionReadingRates(
        blockRate = blockRate,
        blockPace = blockRate.inverse,
        wordRate = progress.wordsRead / progress.cumulativeReadingTime
      )
    }

    val bookReadingRates: BookReadingRates = BookReadingRates(
      blockDailyRate = BlockDailyRate.from(progress.locationsRead, progress.calendarDuration)
    )

    val estimates: Estimates = {
      val scaleFactor: Ratio = Ratio(progress.completed.inverse.value - 1.0)
      Estimates(
        progress.cumulativeReadingTime * scaleFactor,
        calendarDateStats.start + (progress.calendarDuration * scaleFactor)
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
