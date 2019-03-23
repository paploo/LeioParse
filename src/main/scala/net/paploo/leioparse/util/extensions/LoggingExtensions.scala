package net.paploo.leioparse.util.extensions

import org.slf4j.Logger
import net.paploo.leioparse.util.extensions.ExtendedAny.Implicits._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object LoggingExtensions {

  trait Implicits {
    import scala.language.implicitConversions
    implicit def valueToValueLoggerExtensions[A](a: A): ValueLoggerExtensions[A] = new ValueLoggerExtensions[A](a)
    implicit def futureToLoggerExtensions[A](f: Future[A]): FutureLoggerExtensions[A] = new FutureLoggerExtensions[A](f)
    implicit def tryToLoggerExtensions[A](t: Try[A]): TryLoggerExtensions[A] = new TryLoggerExtensions[A](t)
  }
  object Implicits extends Implicits

}

class ValueLoggerExtensions[+A](val value: A) extends AnyVal {

  def log[B](f: A => B)(implicit logger: Logger): A = value.tap(a => logger.info(f(a).toString))

}

class FutureLoggerExtensions[+A](val toFuture: Future[A]) extends AnyVal {

  def log[B](f: A => B)(implicit logger: Logger, ec: ExecutionContext): Future[A] = toFuture andThen {
    case Success(a) => logger.info(f(a).toString)
    case Failure(th) => logger.error(th.getMessage, th)
  }

  def logWith[B](pf: PartialFunction[Try[A], B])(implicit logger: Logger, ec: ExecutionContext): Future[A] =
    toFuture andThen (pf andThen (_.toString))

}

class TryLoggerExtensions[+A](val toTry: Try[A]) extends AnyVal {

  def log[B](f: A => B)(implicit logger: Logger): Try[A] = toTry.tap {
    case Success(a) => logger.info(f(a).toString)
    case Failure(th) => logger.error(th.getMessage, th)
  }

  def logWith[B](pf: PartialFunction[Try[A], B])(implicit logger: Logger): Try[A] = toTry.tap(pf andThen (_.toString))

}
