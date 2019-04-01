package net.paploo.leioparse.test

import org.scalatest

import scala.annotation.tailrec

trait LineDiffSupport {
  self: TestSpec =>

  def assertNoLineDifference(lines: Seq[String], expectedLines: Seq[String]): scalatest.Assertion =
    LineDiffSupport.diff(lines, expectedLines) should === (LineDiffSupport.DiffReport.empty)

}

object LineDiffSupport {

  case class DiffReport(missingLines: Seq[Line],
                        extraLines: Seq[Line]) {
    val isAMatch: Boolean = extraLines.isEmpty && missingLines.isEmpty
    override def toString: String = s"$productPrefix(\n\textraLines  : $extraLines,\n\tmissingLines: $missingLines\n)"
  }

  object DiffReport {
    def empty = DiffReport(Seq.empty, Seq.empty)
  }

  /**
    * Wraps a line value with its metadata.
    *
    * Algorithms depend on the equality check being just on the value, so metadata (e.g. line number) are put
    * in an extra parameters list where it doesn't affect the output.
    */
  case class Line(value: String)(val lineNumber: Int) {
    def toTuple: (Int, String) = (lineNumber, value)
    override def toString: String = s"$productPrefix($lineNumber: $value)"
  }

  def diff(lines: Seq[String], expectedLines: Seq[String]): DiffReport = {
    val lineList: List[Line] = lines.zipWithIndex.map { case (s, i) => Line(s)(i+1) }.toList
    val expectedLineList: List[Line] = expectedLines.zipWithIndex.map { case (s, i) => Line(s)(i+1) }.toList

    diffWithOrderedRecursionStrategy(lineList, expectedLineList)
  }

  /**
    * This starategy is simplistic, losing line order context and unable to guard against duplicate lines.
    *
    * This works best for CSV files, where lines are expected to be unique and hold no ordering context.
    */
  private[this] def diffWithSetStrategy(lineSet: Set[Line], expectedLines: Set[Line]): DiffReport = DiffReport(
    missingLines = (expectedLines diff lineSet).toSeq.sortBy(_.lineNumber),
    extraLines = (lineSet diff expectedLines).toSeq.sortBy(_.lineNumber)
  )

  /**
    * This strategy more properly does a diff, by traversing down the left list, looking for additions and removals.
    *
    * It does this by, for each element of the left list:
    * 1. Checking to see if it can be found in the remainder of the right list, such that if it does find it, it assumes
    *    that anything above it were added lines, advances both lists to "consume" the found value, and uses the
    *    remaining right lines for comparisons with further elements from the left, or
    * 2. It can't find the left element, in which case it must've been removed, so it consumes it off, marks that it
    *    was seen as removed, and then goes on to the next element.
    */
  private[this] def diffWithOrderedRecursionStrategy(lines: List[Line], expectedLines: List[Line]): DiffReport = {

    /**
      * Given a list (which may be empty), split into two lists on the pivot value (if found), and return if the pivot was found.
      * @return (listOfBeforeFound, maybeFoundElement, listOfAfterFound)
      */
    def splitOn[A](pivot: A)(list: List[A]): (List[A], Option[A], List[A]) =
      Option(list.indexOf(pivot)).filter(_ >= 0).map(index => list.splitAt(index)).map {
        case (before, atAndAfter) => (before, atAndAfter.headOption, atAndAfter.tail)
      }.getOrElse((list, None, Nil))

    @tailrec
    def diff(leftLines: List[Line], rightLines: List[Line], removedLines: Vector[Line], addedLines: Vector[Line]): (Seq[Line], Seq[Line]) = leftLines match {
      case leftHead :: leftTail =>
        splitOn(leftHead)(rightLines) match {
          case (added, Some(_), rightTail) => diff(leftTail, rightTail, removedLines, addedLines ++ added)
          case (_, None, _) => diff(leftTail, rightLines, removedLines :+ leftHead, addedLines)
        }
      case Nil =>
        (removedLines, addedLines ++ rightLines)
    }

    val (missingLines, extraLines) = diff(lines, expectedLines, Vector.empty, Vector.empty)
    DiffReport(missingLines = missingLines, extraLines = extraLines)
  }

}

class LineDiffSupportTest extends TestSpec {

  import LineDiffSupport._

  describe("diffing") {

    it("should calculate that a set of lines matches itself") {
      val lines: Seq[String] = Seq("A", "B", "C", "D", "E", "F", "G", "H")
      LineDiffSupport.diff(lines, lines) should === (DiffReport.empty)
    }

    it("should work with an empty list of lines") {
      LineDiffSupport.diff(Nil, Nil) should === (DiffReport.empty)
    }

    it("should correctly calculate the added and removed lines") {
      val left: Seq[String] = Seq("A", "B", "C", "D", "E", "F", "G", "H", "I")
      val right: Seq[String] = Seq("B", "C", "G", "D", "D1", "D2", "E", "F1")

      val diffReport = LineDiffSupport.diff(left, right)

      val expectedMissing = Vector(Line("A")(1), Line("F")(6), Line("G")(7), Line("H")(8), Line("I")(9))
      val expectedExtra = Vector(Line("G")(3), Line("D1")(5), Line("D2")(6), Line("F1")(8))

      //Check values and line numbers
      diffReport.missingLines.map(_.toTuple) should === (expectedMissing.map(_.toTuple))
      diffReport.extraLines.map(_.toTuple) should === (expectedExtra.map(_.toTuple))
    }

    it("should correctly calculate if bracketing changed") {
      val left: Seq[String] = Seq("{", "B", "}")
      val right: Seq[String] = Seq("{", "{", "B", "}", "}")

      val diffReport = LineDiffSupport.diff(left, right)

      diffReport.missingLines should === (Vector())
      diffReport.extraLines.map(_.toTuple) should === (Vector(Line("{")(2), Line("}")(5)).map(_.toTuple))
    }

  }

}
