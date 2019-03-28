package net.paploo.leioparse.util.extensions

import org.slf4j.{Logger, LoggerFactory}
import net.paploo.leioparse.util.extensions.ExtendedAny.Implicits._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object LoggingExtensions {

  trait Logging {
    implicit val logger: Logger = LoggerFactory.getLogger(getClass)
  }

  trait Implicits {
    import scala.language.implicitConversions
    implicit def valueToValueLoggerExtensions[A](a: A): ValueLoggerExtensions[A] = new ValueLoggerExtensions[A](a)
    implicit def throwableToValueLoggerExtensions[A](th: Throwable): ThrowableLoggerExtensions[A] = new ThrowableLoggerExtensions[A](th)
    implicit def futureToLoggerExtensions[A](f: Future[A]): FutureLoggerExtensions[A] = new FutureLoggerExtensions[A](f)
    implicit def tryToLoggerExtensions[A](t: Try[A]): TryLoggerExtensions[A] = new TryLoggerExtensions[A](t)
  }
  object Implicits extends Implicits

}

case class ValueLoggerExtensions[+A](value: A) extends AnyVal {
  def log[B](f: A => B)(implicit logger: Logger): A = value.tap(a => logger.info(f(a).toString))
}

case class ThrowableLoggerExtensions[+A](toThrowable: Throwable) extends AnyVal {
  def log[B](f: Throwable => B)(implicit logger: Logger): Throwable = toThrowable.tap(th => logger.error(f(th).toString, th))
}

case class FutureLoggerExtensions[+A](toFuture: Future[A]) extends AnyVal {

  def log[B](f: A => B)(implicit logger: Logger, ec: ExecutionContext): Future[A] = toFuture andThen {
    case Success(a) => ValueLoggerExtensions(a).log(f)
    case Failure(th) => ThrowableLoggerExtensions(th).log(identity)
  }

  def logWith[B](pf: PartialFunction[Try[A], Try[B]])(implicit logger: Logger, ec: ExecutionContext): Future[A] =
    toFuture andThen (pf andThen {
      case Success(b) => ValueLoggerExtensions(b).log(identity)
      case Failure(th) => ThrowableLoggerExtensions(th).log(identity)
    })

}

case class TryLoggerExtensions[+A](toTry: Try[A]) extends AnyVal {

  def log[B](f: A => B)(implicit logger: Logger): Try[A] = toTry.tap {
    case Success(a) => ValueLoggerExtensions(a).log(f)
    case Failure(th) => ThrowableLoggerExtensions(th).log(identity)
  }

  def logWith[B](pf: PartialFunction[Try[A], Try[B]])(implicit logger: Logger): Try[A] = toTry.tap(
    t => TryLoggerExtensions(pf(t)).log(identity)
  )

}
