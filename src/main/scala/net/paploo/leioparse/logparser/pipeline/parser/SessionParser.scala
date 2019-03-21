package net.paploo.leioparse.logparser.pipeline.parser

import net.paploo.leioparse.data.core.Book
import net.paploo.leioparse.logparser.data.LeioSession
import net.paploo.leioparse.logparser.pipeline.LeioParsePipeline.Row
import net.paploo.leioparse.logparser.pipeline.LeioParser
import net.paploo.leioparse.logparser.pipeline.LeioParser.ParseTools
import net.paploo.leioparse.util.quantities.{DateTime, Location, TimeSpan}

import scala.util.{Success, Try}

private class SessionParser extends LeioParser[LeioSession] with ParseTools {
  import SessionParser.Keys._

  override def apply(row: Row): Try[LeioSession] = for {
    bookTitle <- row.extractRequired[Book.Title](BookTitle)
    startedOn <- extractStartedOn(row)
    finishedOn <- row.extractRequired[DateTime](FinishedOn)
    firstPage <- row.extractRequired[Location](FirstPage)
    lastPage <- row.extractRequired[Location](LastPage)
  } yield LeioSession(
    bookTitle,
    startedOn,
    finishedOn,
    firstPage,
    lastPage
  )

  //When Leio gets a manually entered record with a time and duration, the first page is missing, so sometimes we have to infer.
  private[this] def extractStartedOn(row: Row): Try[DateTime] = row.extract[DateTime](StartedOn) match {
    case Some(dt) => Success(dt)
    case None => for {
      finishedOn <- row.extractRequired[DateTime](FinishedOn)
      duration <- row.extractRequired[TimeSpan](SessionDuration)
    } yield finishedOn - duration
  }

}

private object SessionParser {

  val apply: LeioParser[LeioSession] = new SessionParser

  object Keys {
    val BookTitle = Row.Key("Book")
    val StartedOn = Row.Key("Started On")
    val FinishedOn = Row.Key("Finished On")
    val FirstPage = Row.Key("First Page")
    val LastPage = Row.Key("Last Page")
    val SessionDuration = Row.Key("Duration")
  }

}

