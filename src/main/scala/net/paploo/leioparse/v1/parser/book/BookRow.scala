package net.paploo.leioparse.v1.parser.book

import net.paploo.leioparse.v1.parser.Row

case class BookRow(toMap: Map[BookRow.Column, String]) extends Row[BookRow.Column]

object BookRow {

  def fromRaw(map: Map[String, String]): BookRow =
    Row.fromRaw(BookRow.apply, Column.apply)(map)

  sealed trait Column
  object Column {
    case object Title extends Column
    case object Author extends Column
    case class Unknown(header: String) extends Column

    def apply(header: String): Column = header match {
      case "Title" => Title
      case "Author" => Author
      case s => Unknown(s)
    }
  }

}
