package net.paploo.leioparse.parser

import java.time.{Duration, LocalDateTime}

import net.paploo.leioparse.data.{Book, Session}
import net.paploo.leioparse.parser.Row.Column

class DefaultRowParser(bookParser: String => Book,
                       dateParser: String => LocalDateTime,
                       durationParser: String => Duration,
                       pagesParser: String => Int) extends (Row => Session) {

  def apply(row: Row): Session = Session(getBook(row),
                                         getDate(row),
                                         getDuration(row),
                                         getPages(row))

  private[this] def getBook(row: Row): Book =
    row.convert(Column.Book)(bookParser)

  private[this] def getPages(row: Row): Int =
    row.convert(Column.Pages)(pagesParser)

  private[this] def getDuration(row: Row): Duration =
    row.convert(Column.Duration)(durationParser)

  private[this] def getDate(row: Row): LocalDateTime = backDate(
    row.get(Column.Start).map(dateParser),
    row.convert(Column.End)(dateParser),
    getDuration(row)
  )

  private[this] def backDate(startedOn: Option[LocalDateTime], finishedOn: => LocalDateTime, duration: => Duration) =
    startedOn getOrElse finishedOn.minus(duration)

}