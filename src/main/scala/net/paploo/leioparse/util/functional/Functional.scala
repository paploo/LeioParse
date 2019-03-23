package net.paploo.leioparse.util.functional

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

}

object Functional extends Functional
