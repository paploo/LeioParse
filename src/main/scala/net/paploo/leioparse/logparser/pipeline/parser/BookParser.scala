package net.paploo.leioparse.logparser.pipeline.parser

import net.paploo.leioparse.data.core.Book
import net.paploo.leioparse.logparser.data.LeioBook
import net.paploo.leioparse.logparser.pipeline.LeioParsePipeline.Row
import net.paploo.leioparse.logparser.pipeline.LeioParser
import net.paploo.leioparse.logparser.pipeline.LeioParser.ParseTools
import net.paploo.leioparse.util.quantities.Location

import scala.util.Try

private class BookParser extends LeioParser[LeioBook] with ParseTools {
  import BookParser.Keys._

  override def apply(row: Row): Try[LeioBook] = for {
    title <- row.extractRequired[Book.Title](Title)
    firstPage <- row.extractRequired[Location](FirstPage)
    lastPage <- row.extractRequired[Location](LastPage)
  } yield LeioBook(
    title = title,
    firstPage = firstPage,
    lastPage = lastPage
  )

}

private object BookParser {

  val apply: LeioParser[LeioBook] = new BookParser

  object Keys {
    val Title = Row.Key("Title")
    val FirstPage = Row.Key("First Page")
    val LastPage = Row.Key("Last Page")
  }

}