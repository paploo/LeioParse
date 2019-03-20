package net.paploo.leioparse.data.core

case class BookSessions(book: Book,
                        sessions: Seq[Session],
                        stats: Option[BookStatistics])

object BookSessions {

  def from(book: Book, sessions: Seq[Session]): BookSessions = apply(
    book,
    sessions,
    BookStatistics.from(book, sessions)
  )

}
