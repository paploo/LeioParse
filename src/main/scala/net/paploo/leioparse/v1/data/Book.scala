package net.paploo.leioparse.v1.data

case class Book(id: Book.Id,
                title: Book.Title,
                wordsPerPage: Option[Book.WordsPerPage])

object Book {

  val unknown: Book = Book(Book.Id.unknown, Title("Unknown"), None)

  case class Id(value: Int) extends AnyVal

  object Id {
    val unknown: Id = Id(-1)
  }

  case class Title(value: String) extends AnyVal

  case class WordsPerPage(value: Int) extends AnyVal

}
