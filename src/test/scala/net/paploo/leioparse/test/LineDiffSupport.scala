package net.paploo.leioparse.test

import org.scalatest

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
    val lineSet: Set[Line] = lines.zipWithIndex.map { case (s, i) => Line(s)(i+1) }.toSet
    val expectedLineSet: Set[Line] = expectedLines.zipWithIndex.map { case (s, i) => Line(s)(i+1) }.toSet

    val extraLines = (lineSet diff expectedLineSet).toSeq.sortBy(_.lineNumber)
    val missingLines = (expectedLineSet diff lineSet).toSeq.sortBy(_.lineNumber)

    DiffReport(extraLines, missingLines)
  }

}
