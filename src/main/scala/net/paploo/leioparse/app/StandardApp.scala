package net.paploo.leioparse.app

import cats.implicits._
import net.paploo.leioparse.app.App.Result
import net.paploo.leioparse.bookoverlayparser.BookOverlayParser
import net.paploo.leioparse.processing.{BookReportAssembler, BookReportParser}
import net.paploo.leioparse.data.core.BookReport
import net.paploo.leioparse.leiologparser.LeioLogParser
import net.paploo.leioparse.formatter.formatters.PrettyFormatter
import net.paploo.leioparse.formatter.{Formatter, FormatterComposer, Outputter}
import net.paploo.leioparse.util.extensions.Implicits._
import net.paploo.leioparse.util.extensions.LoggingExtensions.Logging

import scala.concurrent.{ExecutionContext, Future}

trait StandardApp extends App[Seq[BookReport]] with Logging {

  override def run(args: AppArgs)(implicit ec: ExecutionContext): Future[Result[Seq[BookReport]]] = for {
    leioLogParser <- leioLogParser(args)
    bookOverlayParser <- bookOverlayParser(args)
    reports <- parse(leioLogParser, bookOverlayParser)
    result <- write(reports)(args)
  } yield Result(reports) tap (r => logger.info(r.toSeq.show))

  def leioLogParser(args: AppArgs)(implicit ec: ExecutionContext): Future[LeioLogParser] =
    Future.successful(LeioLogParser.fromPath(args.dataDirPath))

  def bookOverlayParser(args: AppArgs)(implicit ec: ExecutionContext): Future[BookOverlayParser] =
    Future.successful(BookOverlayParser(args.bookOverlayPath))

  def parse(leioLogParser: LeioLogParser, bookOverlayParser: BookOverlayParser)(implicit ec: ExecutionContext): Future[Seq[BookReport]] =
    BookReportParser(leioLogParser, bookOverlayParser, BookReportAssembler.default).parse

  def formatter(args: AppArgs)(implicit ec: ExecutionContext): Future[Formatter[Unit]] = Future(FormatterComposer(new PrettyFormatter).run)

  def write(reports: Seq[BookReport])(args: AppArgs)(implicit ec: ExecutionContext): Future[Unit] = for {
    formatter <- formatter(args)
    outputResult <- Outputter.formatToStdOut(formatter).apply(reports)
  } yield outputResult

}
object StandardApp extends StandardApp
