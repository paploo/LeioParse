package net.paploo.leioparse.util.extensions

import cats.data.Kleisli
import cats.{Always, Applicative, Eval, FlatMap, Functor, Monad, Monoid, Traverse}
import cats.implicits._

object ExtendedSeq {

  trait SeqMonoid[A] extends Monoid[Seq[A]] {
    override def empty: Seq[A] = Seq.empty

    override def combine(x: Seq[A], y: Seq[A]): Seq[A] = x ++ y
  }

  trait SeqCats extends Monad[Seq] with FlatMap[Seq] with Applicative[Seq] with Traverse[Seq] with Functor[Seq] {

    override def map[A, B](fa: Seq[A])(f: A => B): Seq[B] = fa map f

    override def foldLeft[A, B](fa: Seq[A], b: B)(f: (B, A) => B): B = fa.foldLeft(b)(f)

    override def foldRight[A, B](fa: Seq[A], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] = fa.foldRight(lb)(f)

    override def pure[A](x: A): Seq[A] = Seq(x)

    override def flatMap[A, B](fa: Seq[A])(f: A => Seq[B]): Seq[B] = fa flatMap f

    //The impl from List is very list specific!
    //TODO: Make an implementation that doesn't go through List later.
    override def tailRecM[A, B](a: A)(f: A => Seq[Either[A, B]]): Seq[B] = FlatMap[List].tailRecM(a)(x => f(x).toList)

    //Stolen from the impl for List, but without further thought for optimization!
    //TODO: Optimize this implementation for Seq.
    override def traverse[G[_], A, B](fa: Seq[A])(f: A => G[B])(implicit G: Applicative[G]): G[Seq[B]] =
      foldRight[A, G[Seq[B]]](fa, Always(G.pure(List.empty))) { (a, lglb) =>
        G.map2Eval(f(a), lglb)(_ +: _)
      }.value
  }
  object SeqCats extends SeqCats

  trait Implicits {
    implicit def seqMonoid[A]: Monoid[Seq[A]] = new SeqMonoid[A] {}

    implicit val seqCats: Monad[Seq] with Traverse[Seq] with Functor[Seq] = SeqCats
  }
  object Implicits extends Implicits

}
