package net.paploo.leioparse.app

import cats.Show
import net.paploo.leioparse.app.App.Result

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

    import cats.implicits._
    implicit def ShowResult[A](implicit showA: Show[A]): Show[Result[A]] =
      result => result.toSeq.map(_.show).mkString(s"Result(\n\t", ",\n\t", "\n)")

  }

  private[App] case class CompositeApp[+A](first: App[A], second: App[A]) extends App[A] {
    override def run(args: AppArgs)(implicit ec: ExecutionContext): Future[Result[A]] = for {
      result1 <- first.run(args)
      result2 <- second.run(args)
    } yield result1 ++ result2
  }



}
