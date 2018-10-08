package net.paploo.leioparse.parser.book

import net.paploo.leioparse.data.Book
import net.paploo.leioparse.parser.book.BookRow.Column

class DefaultBookRowParser(bookNameParser: String => String,
                           bookAuthorParser: String => String) extends (BookRow => Book.Data) {

  override def apply(row: BookRow): Book.Data = Book.Data(
    getTitle(row),
    getWordsPerPage(row)
  )

  def getTitle(row: BookRow): String =
    row.convert(Column.Title)(bookNameParser)

  def getAuthor(row: BookRow): String =
    row.convert(Column.Author)(bookAuthorParser)

  def getWordsPerPage(row: BookRow): Option[Int] =
    DefaultBookRowParser.bookWordsPerPage.get(getTitle(row))

}

object DefaultBookRowParser {

  //TODO: Load this from a file and supply the data as a constructor argument
  val bookWordsPerPage: Map[String, Int] = Map(
    "Relic Worlds 1" -> 333, //Just use Relic Worlds 2 counts for the moment.
    "Relic Worlds 2" -> (326+371+303)/3 //Counts from a few typical looking pages
  )

}

