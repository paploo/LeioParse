package net.paploo.leioparse.app

import java.nio.file.Paths

import net.paploo.leioparse.app.App.Result
import net.paploo.leioparse.bookoverlayparser.BookOverlayParser
import net.paploo.leioparse.bookoverlayparser.BookOverlayParser.BookOverlay
import net.paploo.leioparse.leiologparser.LeioLogParser
import net.paploo.leioparse.util.extensions.LoggingExtensions.Implicits._
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContext, Future}

trait App extends (AppArgs => Future[Result]) {
  def run(args: AppArgs): Future[Result]
  override def apply(args: AppArgs): Future[Result] = run(args)
}

object App {

  case class Result(value: Any) //TODO: Figure out a meaningful result.

}

object ParseOverlayAndLogApp extends App {

  implicit val ec: ExecutionContext = ExecutionContext.global

  implicit val logger: Logger = LoggerFactory.getLogger(getClass)

  override def run(args: AppArgs): Future[Result] = {
    import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
    //val s = Seq(BookOverlay.apply("Relic Worlds 1", Some(1), 333), BookOverlay("Alpha", None, 0))
    //s.asJson.log(identity)

    val fp = Paths.get(".", "books.json").normalize()
    BookOverlayParser(fp).parse.log(identity).map(Result.apply)
  }
}

object ParseLeioFilesAndLogApp extends App {

  implicit val ec: ExecutionContext = ExecutionContext.global

  implicit val logger: Logger = LoggerFactory.getLogger(getClass)

  override def run(args: AppArgs): Future[Result] = {
    args.log(identity)
    val parser = LeioLogParser.forPath(args.dataDirPath)
    for {
      books <- parser.parseBooks
      sessions <- parser.parseSessions
      _ = books.log(_.mkString("\n"))
      _ = sessions.log(_.mkString("\n"))
    } yield Result( (books, sessions) )
  }.log(r => s"result = $r")

}