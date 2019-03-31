package net.paploo.leioparse.util.extensions

import cats.Traverse
import net.paploo.leioparse.test.TestSpec
import cats.implicits._
import cats.kernel.Monoid

import scala.util.{Failure, Success, Try}

class CatsSeqImplementationTest extends TestSpec with CatsSeqImplementation.Implicits {

  describe("monoid") {

    it("should have an empty") {
      Monoid.empty[Seq[Int]] should === (Seq.empty[Int])
    }

    it("should combine") {
      Monoid.combine(Seq(1, 2), Seq(3, 4)) should === (Seq(1, 2, 3, 4))
    }

  }

  describe("traverse") {

    it("should sequence a Seq[Try[A]] to a Try[Seq[A]] when all values are Success") {
      val seqOfTry: Seq[Try[Int]] = Seq(Success(1), Success(2), Success(3))
      val tryOfSeq: Try[Seq[Int]] = Traverse[Seq].sequence(seqOfTry)
      tryOfSeq should === (Success(Seq(1, 2, 3)))
    }

    it("should sequence a Seq[Try[A]] to a Failure with the first failure found") {
      val excp = new Exception("sad face")
      val seqOfTry: Seq[Try[Int]] = Seq(Success(1), Success(2), Failure(excp), Success(3), Try(1/0))
      val tryOfSeq: Try[Seq[Int]] = Traverse[Seq].sequence(seqOfTry)
      tryOfSeq should === (Failure(excp))
    }


  }

}
