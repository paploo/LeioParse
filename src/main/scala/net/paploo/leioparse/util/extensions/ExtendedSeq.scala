package net.paploo.leioparse.util.extensions

import cats.Functor

object ExtendedSeq {

  object Functor extends Functor[Seq] {
    override def map[A, B](fa: Seq[A])(f: A => B): Seq[B] = fa map f
  }

  trait Implicits {
    implicit val SeqFunctor: Functor[Seq] = Functor
  }
  object Implicits extends Implicits

}
