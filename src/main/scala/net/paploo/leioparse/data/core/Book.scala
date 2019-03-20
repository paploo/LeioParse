package net.paploo.leioparse.data.core

import net.paploo.leioparse.util.quantities._

case class Book(id: Book.Id,
                title: Book.Title,
                startLocation: Location,
                lastLocation: Location,
                endLocation: Location,
                averageWordsPerBlock: WordDensity) {
  val length: Blocks = startLocation to endLocation
}

object Book {

  case class Id(value: Int) extends AnyVal

  case class Title(value: String) extends AnyVal

}