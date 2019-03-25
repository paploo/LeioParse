package net.paploo.leioparse.compositor

import net.paploo.leioparse.bookoverlayparser.BookOverlayParser
import net.paploo.leioparse.data.core.BookSessions
import net.paploo.leioparse.leiologparser.LeioLogParser

import scala.concurrent.{ExecutionContext, Future}

trait BookSessionsCombinedParser {
  def parse(implicit ec: ExecutionContext): Future[Seq[BookSessions]]
}

object BookSessionsCombinedParser {

  def apply(leioLogParser: LeioLogParser, bookOverlayParser: BookOverlayParser, assembler: BookSessionsAssembler): BookSessionsCombinedParser =
    new ParallelReadBookSessionsCombinedParser(leioLogParser, bookOverlayParser, assembler)

  class ParallelReadBookSessionsCombinedParser(leioLogParser: LeioLogParser, bookOverlayParser: BookOverlayParser, assembler: BookSessionsAssembler) extends BookSessionsCombinedParser {

    override def parse(implicit ec: ExecutionContext): Future[Seq[BookSessions]] = {
      val leioSessionsFuture = leioLogParser.parseSessions
      val leioBooksFuture = leioLogParser.parseBooks
      val bookOverlaysFuture = bookOverlayParser.parse

      for {
        leioSessions <- leioSessionsFuture
        leioBooks <- leioBooksFuture
        bookOverlays <- bookOverlaysFuture
      } yield assembler.assemble(leioSessions, leioBooks, bookOverlays)
    }

  }

}