package net.paploo.leioparse.app

import cats.implicits._
import net.paploo.leioparse.app.App.Result
import net.paploo.leioparse.bookoverlayparser.BookOverlayParser
import net.paploo.leioparse.processing.{BookReportAssembler, BookReportParser}
import net.paploo.leioparse.data.core.BookReport
import net.paploo.leioparse.leiologparser.LeioLogParser
import net.paploo.leioparse.formatter.formatters.{JsonFormatter, DebugFormatter}
import net.paploo.leioparse.formatter.{Formatter, FormatterComposer, Outputter}
import net.paploo.leioparse.util.extensions.Implicits._
import net.paploo.leioparse.util.extensions.LoggingExtensions.Logging

import scala.concurrent.{ExecutionContext, Future}

trait StandardApp extends App[Seq[BookReport]] with Logging {

  override def run(args: AppArgs)(implicit ec: ExecutionContext): Future[Result[Seq[BookReport]]] = runImplicitly(args, ec)

  def runImplicitly(implicit args: AppArgs, ec: ExecutionContext): Future[Result[Seq[BookReport]]] = for {
    leioLogParser <- leioLogParser
    bookOverlayParser <- bookOverlayParser
    reports <- parse(leioLogParser, bookOverlayParser)
    result <- write(reports)
  } yield Result(reports) tap (r => logger.debug(r.toSeq.show))

  def leioLogParser(implicit args: AppArgs, ec: ExecutionContext): Future[LeioLogParser] =
    Future.successful(LeioLogParser.fromPath(args.dataDirPath))

  def bookOverlayParser(implicit args: AppArgs, ec: ExecutionContext): Future[BookOverlayParser] =
    Future.successful(BookOverlayParser(args.bookOverlayPath))

  def parse(leioLogParser: LeioLogParser, bookOverlayParser: BookOverlayParser)(implicit args: AppArgs, ec: ExecutionContext): Future[Seq[BookReport]] =
    BookReportParser(leioLogParser, bookOverlayParser, BookReportAssembler.default).parse

  def formatter(implicit args: AppArgs, ec: ExecutionContext): Future[Formatter[Unit]] = Future(FormatterComposer(new JsonFormatter).run)

  def write(reports: Seq[BookReport])(implicit args: AppArgs, ec: ExecutionContext): Future[Unit] = for {
    formatter <- formatter
    outputResult <- Outputter.formatToStdOut(formatter).apply(reports) //TODO: switch outputter type based on args.
  } yield outputResult

}
object StandardApp extends StandardApp
