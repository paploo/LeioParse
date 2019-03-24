package net.paploo.leioparse.app

import java.nio.file.Paths

import net.paploo.leioparse.app.App.Result
import net.paploo.leioparse.bookoverlayparser.BookOverlayParser
import net.paploo.leioparse.data.leiofile.{LeioBook, LeioSession}
import net.paploo.leioparse.data.overlay.BookOverlay
import net.paploo.leioparse.leiologparser.LeioLogParser
import net.paploo.leioparse.util.extensions.LoggingExtensions.Implicits._
import net.paploo.leioparse.util.extensions.LoggingExtensions.Logging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait App[A] extends (AppArgs => Future[Result[A]]) {
  def run(args: AppArgs): Future[Result[A]]
  override def apply(args: AppArgs): Future[Result[A]] = run(args)
}

object App {

  /**
    * Encapsulates the result values of one or more apps.
    */
  case class Result[+A](toSeq: Try[Seq[A]]) {
    def ++[A1 >: A](other: Result[A1]): Result[A1] = Result(for {
      res1 <- toTry
      res2 <- other.toTry
    } yield res1 ++ res2)
  }

  object Result {
    def pure[A](a: => A): Result[A] = Result(Try(List(a)))
    def fromTry[A](t: Try[A]): Result[A] = Result(t.map(_ :: Nil))
  }

  trait FutureContext {
    self: App[_] =>
    implicit val executionContext: ExecutionContext = ExecutionContext.global
  }

  /**
    * Same as:
    * {{{
    * args => (for {
    *   result1 <- Kleisli(firstApp)
    *   result2 <- Kleisli(secondApp)
    * } yield result1 ++ result2).run(args)
    * }}}
    */
  def compose[A](firstApp: App[A], secondApp: App[A])(implicit ec: ExecutionContext): App[A] = args => for {
    result1 <- firstApp(args)
    result2 <- secondApp(args)
  } yield result1 ++ result2



}

object ParseOverlayAndLogApp extends App[Seq[BookOverlay]] with App.FutureContext with Logging {

  override def run(args: AppArgs):  Future[Result[Seq[BookOverlay]]] = {

    val fp = Paths.get(".", "books.json").normalize()
    BookOverlayParser(fp).parse.log(identity).map(Result.pure)
  }

}

object ParseLeioFilesAndLogApp extends App[Result[(Seq[LeioBook], Seq[LeioSession])]] with App.FutureContext with Logging {

  override def run(args: AppArgs): Future[Result[(Seq[LeioBook], Seq[LeioSession])]] = {
    args.log(identity)
    val parser = LeioLogParser.forPath(args.dataDirPath)
    for {
      books <- parser.parseBooks
      sessions <- parser.parseSessions
      _ = books.log(_.mkString("\n"))
      _ = sessions.log(_.mkString("\n"))
    } yield Result.pure( (books, sessions) )
  }.log(r => s"result = $r")

}