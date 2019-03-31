package net.paploo.leioparse.util.extensions

import net.paploo.leioparse.util.extensions.ExtendedFuture.TryTimeout

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Try}

class ExtendedFuture[+A](toFuture: Future[A]) {

  def toTry( implicit timeout: TryTimeout): Try[A] = {
    Await.ready(toFuture, timeout.toDuration)
    toFuture.value getOrElse Failure(new RuntimeException(s"Cannot convert to Try, ready future value is missing!"))
  }

}

object ExtendedFuture {

  def apply[A](f: Future[A]): ExtendedFuture[A] = new ExtendedFuture[A](f)

  case class TryTimeout(toDuration: Duration) extends AnyVal

  trait Implicits {
    import scala.language.implicitConversions
    implicit val defaultTryTimeout: TryTimeout = TryTimeout(Duration(30, "seconds"))
    implicit def durationToTryTimeout(dur: Duration): TryTimeout = TryTimeout(dur)
    implicit def futureToExtended[A](f: => Future[A]): ExtendedFuture[A] = apply(f)
  }
  object Implicits extends Implicits

}
