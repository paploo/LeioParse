package net.paploo.leioparse.test

import org.scalactic.source.Position
import org.scalatest.exceptions.TestFailedException

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

/**
  * Testing trait that is an alternative to teh default ScalaFutures.
  *
  * It's main purpose is to be easier to use with failing operations than the default.
  *
  * This is backwards compatible with ScalaFutures for most uses of `futureValue`.
  */
trait FutureSupport {
  import FutureSupport._
  implicit val futureTestTimeout: FutureTestTimeout = FutureTestTimeout(Duration(2, "sec"))
  implicit def futureToFutureTest[A](f: Future[A]): FutureTest[A] = FutureTest(f)
}

object FutureSupport {

  case class FutureTestTimeout(toDuration: Duration)

  case class FutureTest[+A](toFuture: Future[A]) extends AnyVal {

    def futureTry(implicit timeout: FutureTestTimeout, pos: Position): Try[A] = {
      Await.ready(toFuture, timeout.toDuration)
      toFuture.value.getOrElse(throw new TestFailedException(_ => Some(s"Future unexpectedly did not complete despite blocking until ready!"), None, pos))
    }

    def futureValue(implicit timeout: FutureTestTimeout, pos: Position): A = futureTry match {
      case Success(a) => a
      case Failure(th) => throw new TestFailedException(_ => Some(s"Expected future to be a success but got failure with $th"), Some(th), pos)
    }

    def futureFailure(implicit timeout: FutureTestTimeout, pos: Position): Throwable = futureTry match {
      case Success(a) => throw new TestFailedException(_ => Some(s"Expected future to be a failure but got success with $a"), None, pos)
      case Failure(th) => th
    }

  }

}
