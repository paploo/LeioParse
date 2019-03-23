package net.paploo.leioparse.util.extensions

import net.paploo.leioparse.util.functional.Functional

class ExtendedAny[+A](val value: A) extends AnyVal {

  def tap[B](f: A => B): A = Functional.tap(f)(value)

  def thru[B](f: A => B): B = f(value)

}

object ExtendedAny {

  def apply[A](a: A): ExtendedAny[A] = new ExtendedAny[A](a)

  trait Implicits {
    import scala.language.implicitConversions
    def anyToExtended[A](a: A): ExtendedAny[A] = apply(a)
  }
  object Implicits extends Implicits

}
