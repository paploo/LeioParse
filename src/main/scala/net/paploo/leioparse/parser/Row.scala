package net.paploo.leioparse.parser

import net.paploo.leioparse.parser.Row.Column

case class Row(toMap: Map[Column, String]) extends AnyVal {
  @inline def apply(column: Column): String = toMap(column)
  @inline def get(column: Column): Option[String] = toMap.get(column)

  @inline def convert[B](column: Column)(f: String => B): B = f(apply(column))
}

object Row {

  def fromRaw(map: Map[String, String]): Row =
    Row(map.filter { case (k,v) => !v.isEmpty }.map { case (k,v) => Column(k) -> v })

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
