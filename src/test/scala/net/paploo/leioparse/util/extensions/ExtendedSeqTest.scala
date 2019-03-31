package net.paploo.leioparse.util.extensions

import net.paploo.leioparse.test.TestSpec

class ExtendedSeqTest extends TestSpec with ExtendedSeq.Implicits {

  describe("orderedGroupBy") {

    case class Box[A](k: Int, a: A)

    it("should group by key, preserving visit order of both keys and values within keys") {
      val boxes: Seq[Box[String]] = Seq(Box(1, "One"), Box(2, "Two"), Box(10, "Ten"), Box(1, "Alpha"), Box(10, "T"))
      val expected: Seq[(Int, Seq[Box[String]])] = Seq(1 -> Seq(Box(1, "One"), Box(1, "Alpha")),
                                                       2 -> Seq(Box(2, "Two")),
                                                       10 -> Seq(Box(10, "Ten"), Box(10, "T")))

      boxes.orderedGroupBy(_.k) should === (expected)
    }

    it("should work with a zero length value") {
      Seq.empty[Box[Int]].orderedGroupBy(_.k) should === (Seq.empty)
    }

  }

}
