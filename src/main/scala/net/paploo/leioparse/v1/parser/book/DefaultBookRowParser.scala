package net.paploo.leioparse.v1.parser.book

import net.paploo.leioparse.v1.data.Book
import net.paploo.leioparse.v1.parser.book.BookRow.Column

class DefaultBookRowParser(bookNameParser: String => String,
                           bookAuthorParser: String => String,
                           overlayData: Book.Title => Option[BookRowOverlayData]) extends (BookRow => Book) {

  override def apply(row: BookRow): Book = Book(
    getBookId(row),
    getTitle(row),
    getWordsPerPage(row)
  )

  def getTitle(row: BookRow): Book.Title =
    Book.Title(row.convert(Column.Title)(bookNameParser))

  def getAuthor(row: BookRow): String =
    row.convert(Column.Author)(bookAuthorParser)

  def getBookId(row: BookRow): Book.Id =
    overlayData(getTitle(row)).map(_.bookId).map(Book.Id.apply) getOrElse Book.Id(-getTitle(row).hashCode.abs) //If it's missing from the data list, we assign a predictable *negative* id.

  def getWordsPerPage(row: BookRow): Option[Book.WordsPerPage] =
    overlayData(getTitle(row)).flatMap(_.wordsPerPage).map(Book.WordsPerPage.apply)

}