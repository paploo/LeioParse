package net.paploo.leioparse.leiologparser.pipeline.parser

import net.paploo.leioparse.data.core.Book
import net.paploo.leioparse.data.leiofile.LeioSession
import net.paploo.leioparse.leiologparser.pipeline.LeioParsePipeline.Row
import net.paploo.leioparse.leiologparser.pipeline.LeioParser
import net.paploo.leioparse.leiologparser.pipeline.LeioParser.ParseTools
import net.paploo.leioparse.util.quantities.{DateTime, Location, TimeSpan}

import scala.util.{Success, Try}

class SessionParser extends LeioParser[LeioSession] with ParseTools {

  import SessionParser.Keys._

  override def apply(row: Row): Try[LeioSession] = for {
    bookTitle <- row.extractRequired[Book.Title](BookTitle)
    startedOn <- extractStartedOn(row)
    finishedOn <- row.extractRequired[DateTime](FinishedOn)
    duration <- row.extractRequired[TimeSpan](SessionDuration)
    firstPage <- row.extractRequired[Location](FirstPage)
    lastPage <- row.extractRequired[Location](LastPage)
  } yield LeioSession(
    bookTitle,
    startedOn,
    finishedOn,
    duration,
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

object SessionParser extends (() => LeioParser[LeioSession]) {

  override def apply(): LeioParser[LeioSession] = new SessionParser

  object Keys {
    val BookTitle = Row.Key("Book")
    val StartedOn = Row.Key("Started On")
    val FinishedOn = Row.Key("Finished On")
    val FirstPage = Row.Key("First Page")
    val LastPage = Row.Key("Last Page")
    val SessionDuration = Row.Key("Duration")
  }

}

