package net.paploo.leioparse.data.core

import cats.Show
import cats.implicits._
import net.paploo.leioparse.util.extensions.Implicits._

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

  implicit def ShowBookSessions: Show[BookSessions] =
    bs => s"BookSessions(\n\t${bs.book.show},\n\t${bs.sessions.map(_.show).mkString("Seq(\n\t\t", ",\n\t\t", "\n\t)")},\n\t${bs.stats.show}\n)"

}
