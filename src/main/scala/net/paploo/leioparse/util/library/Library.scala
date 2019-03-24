package net.paploo.leioparse.util.library

import cats.data.Kleisli

import scala.language.higherKinds

trait Library[F[+_], -K, +V] extends (K => F[V]) {
  def toKleisli[K1 <: K, V1 >: V]: Kleisli[F, K1, V1] = Kleisli[F, K1, V1](this)
}

object Library {

  type OptionLibrary[-K, +V] = Library[Option, K, V]

  def apply[F[+_], K, V](f: K => F[V]): Library[F, K, V] = key => f(key)

  def fromMap[K, V](map: Map[K, V]): OptionLibrary[K, V] = apply(map.lift)

  //Note: The apply order is taken to let type inference infer the type of K before the extraction function, allowing it to also infer V.
  def fromSeq[K, V](seq: Seq[V])(extractKey: V => K): OptionLibrary[K, V] = fromMap(seq.map(v => extractKey(v) -> v).toMap)

}
