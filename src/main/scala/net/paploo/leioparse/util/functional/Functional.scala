package net.paploo.leioparse.util.functional

import cats.Functor

import scala.language.higherKinds

trait Functional {

  /**
    * Implement the K-Combinator, but apply f for the side-effects.
    */
  def tap[A,B](f: A => B)(a: A): A = {
    f(a)
    a
  }

  /**
    * Swaps the arguments of a given function.
    */
  def swap[A, B, C](f: A => B => C): B => A => C = b => a => f(a)(b)

  /**
    * Lifts a function A => B to F[A] => F[B] for Functor F
    */
  def lift[F[_], A, B](f: A => B)(implicit functor: Functor[F]): F[A] => F[B] = functor.lift(f)

}

object Functional extends Functional
