package net.paploo.leioparse.util.library

import cats.data.Kleisli

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

trait Library[F[+_], -K, +V] extends (K => F[V]) {
  def toKleisli[K1 <: K, V1 >: V]: Kleisli[F, K1, V1] = Kleisli[F, K1, V1](this)
}

object Library {
}

trait OptionLibrary[-K, +V] extends Library[Option, K, V]

trait FutureLibrary[-K, +V] extends (ExecutionContext => K => Future[V]) {
  def run(k: K)(implicit ec: ExecutionContext): Future[V]

  override def apply(ec: ExecutionContext): K => Future[V] = k => run(k)(ec)
}

object FutureLibrary {

  def apply[K, V](lookup: K => Future[V]): FutureLibrary[K, V] = ??? //k => lookup(k)

  def fromFunction[K,V](get: K => V): FutureLibrary[K, V] = ??? //k => Future.successful(get(k))

}
