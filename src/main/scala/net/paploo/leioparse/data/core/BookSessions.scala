package net.paploo.leioparse.data.core

import scala.util.Try

case class BookSessions(book: Book,
                        sessions: Seq[Session],
                        stats: Option[BookStatistics])

object BookSessions {

  def from(book: Book, sessions: Seq[Session]): Try[BookSessions] = for {
    stats <- BookStatistics.from(book, sessions)
  } yield apply(
    book,
    sessions,
    stats
  )

}
