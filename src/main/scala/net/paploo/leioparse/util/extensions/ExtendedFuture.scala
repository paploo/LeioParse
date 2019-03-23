package net.paploo.leioparse.util.extensions

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Try}

class ExtendedFuture[+A](toFuture: Future[A]) {

  def toTry(timeout: Duration): Try[A] = {
    Await.ready(toFuture, timeout)
    toFuture.value getOrElse Failure(new RuntimeException(s"Cannot convert to Try, ready future value is missing!"))
  }

}

object ExtendedFuture {

  def apply[A](f: Future[A]): ExtendedFuture[A] = new ExtendedFuture[A](f)

  trait Implicits {
    import scala.language.implicitConversions
    implicit def futureToExtended[A](f: => Future[A]): ExtendedFuture[A] = apply(f)
  }
  object Implicits extends Implicits

}
