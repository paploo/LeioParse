package net.paploo.leioparse.data.core

import cats.Show
import net.paploo.leioparse.util.quantities._

case class Book(title: Book.Title,
                startLocation: Location,
                endLocation: Location,
                averageWordDensity: WordDensity,
                externalId: Option[Book.ExternalId] = None) {
  val length: Blocks = startLocation to endLocation
}

object Book {

  case class ExternalId(value: String) extends AnyVal

  case class Title(value: String) extends AnyVal

  implicit val ShowBook: Show[Book] = book => (book.productIterator ++ Seq(book.length)).mkString(s"${book.productPrefix}(", ", ", ")")

}