package net.paploo.leioparse.app

import cats.implicits._
import net.paploo.leioparse.app.App.Result
import net.paploo.leioparse.bookoverlayparser.BookOverlayParser
import net.paploo.leioparse.processing.{BookSessionsAssembler, BookSessionsCombinedParser}
import net.paploo.leioparse.data.core.BookSessions
import net.paploo.leioparse.leiologparser.LeioLogParser
import net.paploo.leioparse.util.extensions.Implicits._
import net.paploo.leioparse.util.extensions.LoggingExtensions.Logging

import scala.concurrent.{ExecutionContext, Future}

trait StandardApp extends App[Seq[BookSessions]] with Logging {

  override def run(args: AppArgs)(implicit ec: ExecutionContext): Future[Result[Seq[BookSessions]]] = for {
    leioLogParser <- leioLogParser(args)
    bookOverlayParser <- bookOverlayParser(args)
    bookSessions <- parse(leioLogParser, bookOverlayParser)
  } yield Result(bookSessions) tap (r => logger.info(r.toSeq.show))

  def leioLogParser(args: AppArgs)(implicit ec: ExecutionContext): Future[LeioLogParser] =
    Future.successful(LeioLogParser.fromPath(args.dataDirPath))

  def bookOverlayParser(args: AppArgs)(implicit ec: ExecutionContext): Future[BookOverlayParser] =
    Future.successful(BookOverlayParser(args.bookOverlayPath))

  def parse(leioLogParser: LeioLogParser, bookOverlayParser: BookOverlayParser)(implicit ec: ExecutionContext): Future[Seq[BookSessions]] =
    BookSessionsCombinedParser(leioLogParser, bookOverlayParser, BookSessionsAssembler.default).parse

}
object StandardApp extends StandardApp
