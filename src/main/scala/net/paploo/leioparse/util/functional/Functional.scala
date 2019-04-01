package net.paploo.leioparse.util.functional

trait Functional {

  /**
    * Identity as a lambda function
    */
  def id[A]: A => A = identity

  /**
    * Const ignores the argument and returns the given value.
    */
  def const[A, B](b: B): A => B = _ => b

  /**
    * Implement the K-Combinator, but apply f for the side-effects.
    *
    * Note that this ordering, while it works great in map, requires explicit type
    * annotation when used on its own. This order was chosen because, in cases where
    * it matters, one usually should use the tap method in the extensions package.
    */
  def tap[A, B](f: A => B)(a: A): A = {
    f(a)
    a
  }

  /**
    * Swaps the arguments of a given function.
    */
  def swap[A, B, C](f: A => B => C): B => A => C = b => a => f(a)(b)

}

object Functional extends Functional
