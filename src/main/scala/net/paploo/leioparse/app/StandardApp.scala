package net.paploo.leioparse.app

import cats.implicits._
import net.paploo.leioparse.app.App.Result
import net.paploo.leioparse.app.AppArgs.{FormatterArg, OutputMethod}
import net.paploo.leioparse.bookoverlayparser.BookOverlayParser
import net.paploo.leioparse.processing.{BookReportAssembler, BookReportParser}
import net.paploo.leioparse.data.core.BookReport
import net.paploo.leioparse.leiologparser.LeioLogParser
import net.paploo.leioparse.formatter.formatters.{DebugFormatter, JsonFormatter, LegacyCSVFormatter}
import net.paploo.leioparse.formatter.{Formatter, FormatterComposer, Outputter}
import net.paploo.leioparse.util.extensions.Implicits._
import net.paploo.leioparse.util.extensions.LoggingExtensions.Logging

import scala.concurrent.{ExecutionContext, Future}

/**
  * The "standard" app implementation.
  *
  * This is designed to be state-less, allowing multiple invocations of run, as such, the default reference is a static object.
  */
trait StandardApp extends App[Seq[BookReport]] with Logging {

  override def run(args: AppArgs)(implicit ec: ExecutionContext): Future[Result[Seq[BookReport]]] =
    args.log(a => s"Running with $a").thru(a => runImplicitly(a, ec))

  private[this] def runImplicitly(implicit args: AppArgs, ec: ExecutionContext): Future[Result[Seq[BookReport]]] = for {
    leioLogParser <- leioLogParser
    bookOverlayParser <- bookOverlayParser
    reports <- parse(leioLogParser, bookOverlayParser)
    result <- write(reports)
  } yield Result(reports) tap (r => logger.debug(r.toSeq.show))

  private[this] def leioLogParser(implicit args: AppArgs, ec: ExecutionContext): Future[LeioLogParser] =
    Future.successful(LeioLogParser.fromPath(args.inputDirPath))

  private[this] def bookOverlayParser(implicit args: AppArgs, ec: ExecutionContext): Future[BookOverlayParser] =
    Future.successful(BookOverlayParser(args.bookOverlayPath))

  private[this] def parse(leioLogParser: LeioLogParser, bookOverlayParser: BookOverlayParser)(implicit args: AppArgs, ec: ExecutionContext): Future[Seq[BookReport]] =
    BookReportParser(leioLogParser, bookOverlayParser, BookReportAssembler.default).parse

  private[this] def formatter(implicit args: AppArgs, ec: ExecutionContext): Future[Formatter[Unit]] = Future(FormatterComposer(
    args.formatter match {
      case FormatterArg.Debug => new DebugFormatter
      case FormatterArg.JSON => new JsonFormatter
      case FormatterArg.CSV => new LegacyCSVFormatter //TODO: Create the formatter!
      case FormatterArg.LegacyCSV => new LegacyCSVFormatter
      case _ => new LegacyCSVFormatter
    }
  ).run)

  private[this] def write(reports: Seq[BookReport])(implicit args: AppArgs, ec: ExecutionContext): Future[Unit] = for {
    formatter <- formatter
    outputResult <- (args.outputMethod match {
      case OutputMethod.FilePath(path) => Outputter.formatToPath(path)(formatter)
      case OutputMethod.Lines(promise) => Outputter.formatToLinesPromise(promise)(formatter)
      case _ => Outputter.formatToStdOut(formatter)
    }).apply(reports)
  } yield outputResult

}

object StandardApp extends StandardApp