package net.paploo.leioparse.test

import org.scalatest

import scala.annotation.tailrec

trait LineDiffSupport {
  self: TestSpec =>

  def assertNoLineDifference(lines: Seq[String], expectedLines: Seq[String]): scalatest.Assertion =
    LineDiffSupport.diff(lines, expectedLines) should === (LineDiffSupport.DiffReport.empty)

}

object LineDiffSupport {

  case class DiffReport(extraLines: Seq[Line],
                        missingLines: Seq[Line]) {
    val isAMatch: Boolean = extraLines.isEmpty && missingLines.isEmpty
    override def toString: String = s"$productPrefix(\n\textraLines  : $extraLines,\n\tmissingLines: $missingLines\n)"
  }

  object DiffReport {
    def empty = DiffReport(Seq.empty, Seq.empty)
  }

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
    extraLines = (lineSet diff expectedLines).toSeq.sortBy(_.lineNumber),
    missingLines = (expectedLines diff lineSet).toSeq.sortBy(_.lineNumber)
  )

  private[this] def diffWithOrderedRecursionStrategy(lines: List[Line], expectedLines: List[Line]): DiffReport = {

    /**
      * Given a list (which may be empty), split into two lists on the pivot value (if found), and return if the pivot was found.
      */
    def splitOn[A](pivot: A)(list: List[A]): (List[A], Option[A], List[A]) =
      Option(list.indexOf(pivot)).filter(_ >= 0).map(index => list.splitAt(index)).map {
        case (before, atAndAfter) => (before, atAndAfter.headOption, atAndAfter.tail)
      }.getOrElse((list, None, Nil))

    @tailrec
    def diff(leftLines: List[Line], rightLines: List[Line], addedLines: Vector[Line], removedLines: Vector[Line]): (Seq[Line], Seq[Line]) = leftLines match {
      case leftHead :: leftTail =>
        splitOn(leftHead)(rightLines) match {
          case (added, Some(found), rightTail) => diff(leftTail, rightTail, addedLines ++ added, removedLines)
          case (_, None, rightTail) => diff(leftTail, rightLines, addedLines, removedLines :+ leftHead)
        }
      case Nil =>
        (addedLines ++ rightLines, removedLines)
    }

    val (extraLines, missingLines) = diff(lines, expectedLines, Vector.empty, Vector.empty)
    DiffReport(extraLines, missingLines)
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

      diffReport.missingLines should === (Vector(Line("A")(1), Line("F")(6), Line("G")(7), Line("H")(8), Line("I")(9)))
      diffReport.extraLines should === (Vector(Line("G")(3), Line("D1")(5), Line("D2")(6), Line("F1")(8)))

    }

  }

}
