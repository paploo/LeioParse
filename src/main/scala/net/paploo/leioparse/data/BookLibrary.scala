package net.paploo.leioparse.data

case class BookLibrary(toSeq: Seq[Book]) {

  def findById(id: Book.Id): Option[Book] = toSeq.find(_.id == id)

  def findByName(name: String): Option[Book] = toSeq.find(_.data.title == name)

}

object BookLibrary {

  /**
    * Takes the book's sequence index and the Book, and returns an ID.
    */
  type IdGenerator = Int => Book.Data => Book.Id

  object IdGenerator {
    val byIndex: IdGenerator = index => _ => Book.Id(index + 1)
  }

  /**
    * Given an ordered sequence of books that have no IDs assigned, assign the IDs given the
    * given idGenerator.
    *
    * Since the entries in the sources are usually ordered by date read, index position makes
    * simple and repeatable IDs, however methods built on the book are more stable over file processing.
    */
  def build(idGenerator: IdGenerator)(bookData: Seq[Book.Data]): BookLibrary = BookLibrary(
    (bookData.indices zip bookData).map { case (index, data) => Book(idGenerator(index)(data), data) }
  )

}