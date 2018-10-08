package net.paploo.leioparse.parser.session

import java.time.{Duration, LocalDateTime}

import net.paploo.leioparse.data.{Book, Session}
import net.paploo.leioparse.parser.session.SessionRow.Column

class DefaultSessionRowParser(bookParser: String => Book,
                              dateParser: String => LocalDateTime,
                              durationParser: String => Duration,
                              pagesParser: String => Int) extends (SessionRow => Session) {

  def apply(row: SessionRow): Session = Session(
    getBook(row),
    getDate(row),
    getDuration(row),
    getPages(row)
  )

  private[this] def getBook(row: SessionRow): Book =
    row.convert(Column.Book)(bookParser)

  private[this] def getPages(row: SessionRow): Int =
    row.convert(Column.Pages)(pagesParser)

  private[this] def getDuration(row: SessionRow): Duration =
    row.convert(Column.Duration)(durationParser)

  private[this] def getDate(row: SessionRow): LocalDateTime = backDate(
    row.get(Column.Start).map(dateParser),
    row.convert(Column.End)(dateParser),
    getDuration(row)
  )

  private[this] def backDate(startedOn: Option[LocalDateTime], finishedOn: => LocalDateTime, duration: => Duration) =
    startedOn getOrElse finishedOn.minus(duration)

}