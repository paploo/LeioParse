package net.paploo.leioparse.processing

import net.paploo.leioparse.bookoverlayparser.BookOverlayParser
import net.paploo.leioparse.data.core.BookReport
import net.paploo.leioparse.leiologparser.LeioLogParser

import scala.concurrent.{ExecutionContext, Future}

trait BookReportParser {
  def parse(implicit ec: ExecutionContext): Future[Seq[BookReport]]
}

object BookReportParser {

  def apply(leioLogParser: LeioLogParser, bookOverlayParser: BookOverlayParser, assembler: BookReportAssembler): BookReportParser =
    new ParallelReadBookReportCombinedParser(leioLogParser, bookOverlayParser, assembler)

  class ParallelReadBookReportCombinedParser(leioLogParser: LeioLogParser, bookOverlayParser: BookOverlayParser, assembler: BookReportAssembler) extends BookReportParser {

    override def parse(implicit ec: ExecutionContext): Future[Seq[BookReport]] = {
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