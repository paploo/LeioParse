package net.paploo.leioparse.parser.book

import net.paploo.leioparse.data.Book
import net.paploo.leioparse.parser.book.BookRow.Column

class DefaultBookRowParser(bookNameParser: String => String,
                           bookAuthorParser: String => String,
                           wordsPerPageByBookNameParser: String => Option[Int]) extends (BookRow => Book.Data) {

  override def apply(row: BookRow): Book.Data = Book.Data(
    getTitle(row),
    getWordsPerPage(row)
  )

  def getTitle(row: BookRow): String =
    row.convert(Column.Title)(bookNameParser)

  def getAuthor(row: BookRow): String =
    row.convert(Column.Author)(bookAuthorParser)

  def getWordsPerPage(row: BookRow): Option[Int] =
    wordsPerPageByBookNameParser(getTitle(row))

}

