package net.paploo.leioparse.util.extensions

import cats.{Always, Applicative, CoflatMap, Eval, FlatMap, Foldable, Functor, Monad, Monoid, Semigroup, Show, Traverse}
import cats.implicits._

import scala.annotation.tailrec
import scala.language.higherKinds

object CatsSeqImplementation {

  def SeqShow[A](implicit showA: Show[A]): Show[Seq[A]] = seq => seq.map(showA.show).mkString("Seq(", ", ", ")")

  trait SeqMonoid[A] extends Monoid[Seq[A]] with Semigroup[Seq[A]] {
    override def empty: Seq[A] = Seq.empty

    override def combine(x: Seq[A], y: Seq[A]): Seq[A] = x ++ y
  }

  trait SeqInstances extends Monad[Seq] with FlatMap[Seq] with Applicative[Seq] with Functor[Seq] with Traverse[Seq] with Foldable[Seq] with CoflatMap[Seq] {

    override def map[A, B](fa: Seq[A])(f: A => B): Seq[B] = fa map f

    override def foldLeft[A, B](fa: Seq[A], b: B)(f: (B, A) => B): B = fa.foldLeft(b)(f)

    override def foldRight[A, B](fa: Seq[A], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] = fa.foldRight(lb)(f)

    override def pure[A](x: A): Seq[A] = Seq(x)

    override def flatMap[A, B](fa: Seq[A])(f: A => Seq[B]): Seq[B] = fa flatMap f

    //Note: Impl is an adapted version of the one from Vector, cleaned-up to remove the mutable state.
    //Note: Alternative, if one doesn't mind conversion to a List: FlatMap[List].tailRecM(a)(x => f(x).toList)
    override def tailRecM[A, B](a: A)(f: A => Seq[Either[A, B]]): Seq[B] = {
      val buf = Seq.newBuilder[B]
      val initialState = List(f(a).iterator)

      @tailrec
      def loop(state: List[Iterator[Either[A, B]]]): Unit = state match {
        case Nil => ()
        case h :: tail if h.isEmpty =>
          loop(tail)
        case h :: tail =>
          h.next match {
            case Right(b) =>
              buf += b
              loop(state)
            case Left(a) =>
              loop((f(a).iterator) :: h :: tail)
          }
      }

      loop(initialState)
      buf.result
    }

    //Note: Impl is adapted version as that for List and Vector.
    override def traverse[G[_], A, B](fa: Seq[A])(f: A => G[B])(implicit G: Applicative[G]): G[Seq[B]] =
      foldRight[A, G[Seq[B]]](fa, Always(G.pure(Seq.empty))) { (a, lgsb) =>
        G.map2Eval(f(a), lgsb)(_ +: _)
      }.value

    //TODO: Make an implementation that doesn't translate the starting collection as a list.
    override def coflatMap[A, B](fa: Seq[A])(f: Seq[A] => B): Seq[B] = CoflatMap[List].coflatMap(fa.toList)(f)

  }
  object SeqInstances extends SeqInstances

  trait Implicits {
    implicit def seqShow[A](implicit showA: Show[A]): Show[Seq[A]] = SeqShow[A]
    implicit def seqMonoid[A]: Monoid[Seq[A]] = new SeqMonoid[A] {}
    implicit val seqCats: Monad[Seq] with Traverse[Seq] with Functor[Seq] = SeqInstances
  }
  object Implicits extends Implicits

}

