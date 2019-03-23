package net.paploo.leioparse.app

import net.paploo.leioparse.app.App.Result
import net.paploo.leioparse.leiologparser.LeioLogParser

import scala.concurrent.{ExecutionContext, Future}

class App extends (AppArgs => Future[Result]) {

  implicit val ec: ExecutionContext = ExecutionContext.global

  override def apply(args: AppArgs): Future[Result] = {
    println(args)
    val parser = LeioLogParser.forPath(args.dataDirPath)
    for {
      books <- parser.parseBooks
      sessions <- parser.parseSessions
      _ = println(books.mkString("\n"))
      _ = println(sessions.mkString("\n"))
    } yield Result( (books, sessions) )
  }

}

object App {

  def apply: App = new App

  case class Result(value: Any) //TODO: Figure out a meaningful result.

}