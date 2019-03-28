package net.paploo.leioparse.formatter.formatters

import java.io.PrintWriter

import net.paploo.leioparse.data.core.BookReport
import net.paploo.leioparse.formatter.WriterFormatter
import pprint.PPrinter

class DebugFormatter extends WriterFormatter[Unit] {

  override def writeReports(reports: Seq[BookReport])(implicit writer: PrintWriter): Unit =
    writer.println( PPrinter.BlackWhite.apply(reports, 160, 10000) )

}
