package net.paploo.leioparse.data.core

import cats.Show
import net.paploo.leioparse.util.quantities._

/**
  * Represents a book in the library.
  *
  * @param title              The title of the book; this is also used as the primary key for referencing from sessions.
  * @param startLocation      The first location (e.g. page) of the book (inclusive).
  * @param endLocation        The last page location (e.g. page) read in the book (inclusive).
  * @param averageWordDensity The average number of words per location (e.g. words per page); used to derive word counts.
  * @param externalId         An ID specified in the book overlay library; the meaning is entirely up to whomever created the library.
  */
case class Book(title: Book.Title,
                startLocation: Location,
                endLocation: Location,
                averageWordDensity: WordDensity,
                externalId: Option[Book.ExternalId] = None) {
  val length: Blocks = startLocation to endLocation
  val words: Words = length * averageWordDensity
}

object Book {

  case class ExternalId(value: String) extends AnyVal

  case class Title(value: String) extends AnyVal

  implicit val ShowBook: Show[Book] = book => (book.productIterator ++ Seq(book.length)).mkString(s"${book.productPrefix}(", ", ", ")")

}