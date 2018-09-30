package net.paploo.leioparse.data

case class BookLibrary(toSeq: Seq[Book]) {

  def getOrAdd(bookName: String): (BookLibrary, Book) =
    toSeq.find(_.name == bookName).map(book => (this, book)).getOrElse {
      val book = Book(nextId, bookName)
      (this.copy(toSeq :+ book), book)
    }

  def nextId: Int = if (toSeq.isEmpty) 1 else toSeq.map(_.id).max + 1

}

object BookLibrary {

  def empty: BookLibrary = BookLibrary(Seq.empty)

}