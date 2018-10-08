package net.paploo.leioparse.data

case class Book(id: Book.Id,
                data: Book.Data)

object Book {

  val unknown: Book = Book(Book.Id.unknown, Data("Unknown", None))

  case class Id(value: Int) extends AnyVal

  object Id {
    val unknown: Id = Id(-1)
  }

  case class Data(title: String,
                      worsePerPage: Option[Int])

}
