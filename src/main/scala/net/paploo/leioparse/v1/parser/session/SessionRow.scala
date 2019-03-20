package net.paploo.leioparse.v1.parser.session

import net.paploo.leioparse.v1.parser.Row

case class SessionRow(toMap: Map[SessionRow.Column, String]) extends Row[SessionRow.Column]

object SessionRow {

  def fromRaw(map: Map[String, String]): SessionRow =
    Row.fromRaw(SessionRow.apply, Column.apply)(map)

  sealed trait Column
  object Column {
    case object Book extends Column
    case object Start extends Column
    case object End extends Column
    case object Duration extends Column
    case object Pages extends Column
    case class Unknown(header: String) extends Column

    def apply(header: String): Column = header match {
      case "Book" => Book
      case "Started On" => Start
      case "Finished On" => End
      case "Duration" => Duration
      case "Pages Read" => Pages
      case s => Unknown(s)
    }
  }

}