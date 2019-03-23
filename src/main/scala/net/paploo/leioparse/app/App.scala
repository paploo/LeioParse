package net.paploo.leioparse.app

import net.paploo.leioparse.app.App.Result
import net.paploo.leioparse.leiologparser.LeioLogParser
import net.paploo.leioparse.util.extensions.LoggingExtensions.Implicits._
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContext, Future}

class App extends (AppArgs => Future[Result]) {

  implicit val ec: ExecutionContext = ExecutionContext.global

  implicit val logger: Logger = LoggerFactory.getLogger(getClass)

  override def apply(args: AppArgs): Future[Result] = {
    args.log(identity)
    val parser = LeioLogParser.forPath(args.dataDirPath)
    for {
      books <- parser.parseBooks
      sessions <- parser.parseSessions
      _ = books.log(_.mkString("\n"))
      _ = sessions.log(_.mkString("\n"))
    } yield Result( (books, sessions) )
  }.log(identity)

}

object App {

  def apply: App = new App

  case class Result(value: Any) //TODO: Figure out a meaningful result.

}