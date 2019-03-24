package net.paploo.leioparse.app

import java.nio.file.Paths

import cats.Show
import net.paploo.leioparse.app.App.Result
import net.paploo.leioparse.bookoverlayparser.BookOverlayParser
import net.paploo.leioparse.data.leiofile.{LeioBook, LeioSession}
import net.paploo.leioparse.data.overlay.BookOverlay
import net.paploo.leioparse.leiologparser.LeioLogParser
import net.paploo.leioparse.util.extensions.LoggingExtensions.Implicits._
import net.paploo.leioparse.util.extensions.LoggingExtensions.Logging

import scala.concurrent.{ExecutionContext, Future}

trait App[+A] extends (AppArgs => ExecutionContext => Future[Result[A]]) {

  def run(args: AppArgs)(implicit ec: ExecutionContext): Future[Result[A]]

  override def apply(args: AppArgs): ExecutionContext => Future[Result[A]] = ec => run(args)(ec)

  def andThenRun[A1 >: A](second: App[A1])(implicit ec: ExecutionContext): App[A1] = App.CompositeApp(this, second)
}

object App {

  /**
    * Encapsulates the result values of one or more apps.
    *
    * When apps are composed, each has a singular result stored as a single element in the sequence.
    */
  trait Result[+A] {
    def toSeq: Seq[A]
    def ++[A1 >: A](other: Result[A1]): Result[A1]
  }

  object Result {
    def apply[A](a: A): Result[A] = ResultVector(Vector(a))

    private case class ResultVector[+A](toSeq: Vector[A]) extends Result[A] {
      override def ++[A1 >: A](other: Result[A1]): Result[A1] = ResultVector(toSeq ++ other.toSeq)
    }

    implicit object ShowResult extends Show[Result[_]] {
      override def show(result: Result[_]): String = result.toSeq.mkString(s"Result(\n\t", ",\n\t", "\n)")
    }
  }

  private[App] case class CompositeApp[+A](first: App[A], second: App[A]) extends App[A] {
    override def run(args: AppArgs)(implicit ec: ExecutionContext): Future[Result[A]] = for {
      result1 <- first.run(args)
      result2 <- second.run(args)
    } yield result1 ++ result2
  }



}

object ParseOverlayAndLogApp extends App[Seq[BookOverlay]] with Logging {

  override def run(args: AppArgs)(implicit ec: ExecutionContext):  Future[Result[Seq[BookOverlay]]] = {

    val fp = Paths.get(".", "books.json").normalize()
    BookOverlayParser(fp).parse.log(identity).map(Result.apply)
  }

}

object ParseLeioFilesAndLogApp extends App[(Seq[LeioBook], Seq[LeioSession])] with Logging {

  override def run(args: AppArgs)(implicit ec: ExecutionContext): Future[Result[(Seq[LeioBook], Seq[LeioSession])]] = {
    args.log(identity)
    val parser = LeioLogParser.forPath(args.dataDirPath)
    for {
      books <- parser.parseBooks
      sessions <- parser.parseSessions
      _ = books.log(_.mkString("\n"))
      _ = sessions.log(_.mkString("\n"))
    } yield Result.apply( (books, sessions) )
  }.log(r => s"result = $r")

}