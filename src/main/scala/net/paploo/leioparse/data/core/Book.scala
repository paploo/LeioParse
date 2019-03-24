package net.paploo.leioparse.data.core

import net.paploo.leioparse.util.quantities._

case class Book(identifier: Option[Book.Id] = None,
                title: Book.Title,
                startLocation: Location,
                endLocation: Location,
                averageWordDensity: WordDensity) {
  val length: Blocks = startLocation to endLocation
}

object Book {

  case class Id(value: Int) extends AnyVal

  case class Title(value: String) extends AnyVal

}