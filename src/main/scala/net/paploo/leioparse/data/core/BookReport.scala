package net.paploo.leioparse.data.core

import cats.Show
import cats.implicits._

import scala.util.Try

case class BookReport(book: Book,
                      sessions: Seq[Session],
                      stats: Try[BookStatistics])

object BookReport {

  def from(book: Book, sessions: Seq[Session]): BookReport = apply(
    book,
    sessions,
    BookStatistics.from(book, sessions)
  )

  implicit def ShowBookReport: Show[BookReport] =
    bs => s"BookReport(\n\t${bs.book.show},\n\t${bs.sessions.map(_.show).mkString("Seq(\n\t\t", ",\n\t\t", "\n\t)")},\n\t${bs.stats.show}\n)"

}
