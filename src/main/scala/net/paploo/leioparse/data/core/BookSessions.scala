package net.paploo.leioparse.data.core

import scala.util.Try

case class BookSessions(book: Book,
                        sessions: Seq[Session],
                        stats: Try[BookStatistics])

object BookSessions {

  def from(book: Book, sessions: Seq[Session]): BookSessions = apply(
    book,
    sessions,
    BookStatistics.from(book, sessions)
  )

}
