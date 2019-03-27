package net.paploo.leioparse.formatter.formatters

import java.io.PrintWriter

import net.paploo.leioparse.data.core.{BookReport, BookStatistics}
import net.paploo.leioparse.formatter.WriterFormatter

import scala.util.{Failure, Success, Try}

class PrettyFormatter extends WriterFormatter[Unit] {
  //TODO: Swap out for a nice pretty-print library.

  override def writeReports(reports: Seq[BookReport])(implicit writer: PrintWriter): Unit = reports.foreach {
    report => {
      writer.println(s"${report.productPrefix}(")
      writer.println(s"\t${report.book}")
      writer.println(report.sessions.mkString(s"\tSeq(\n\t\t", ",\n\t\t", "\n\t)"))
      writeStatsTry(report.stats)
      writer.println(s")")
    }
  }

  private[this] def writeStatsTry(triedStatistics: Try[BookStatistics])(implicit writer: PrintWriter): Unit = triedStatistics match {
    case Failure(th) =>
      writer.println(s"\tFailure($th)")
    case Success(stats) =>
      writer.println(s"\tSuccess(")
      writer.println(stats.productIterator.map(_.toString).mkString(s"\t\t${stats.productPrefix()}\n\t\t\t", ",\n\t\t\t", "\n\t\t)"))
      writer.println(s"\t)")
  }

}
