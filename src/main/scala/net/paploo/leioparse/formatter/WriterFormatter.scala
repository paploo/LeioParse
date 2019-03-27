package net.paploo.leioparse.formatter

import java.io.PrintWriter

import net.paploo.leioparse.data.core.BookReport

/**
  * Convenience trait for building formatters with an implicit PrintWriter.
  */
trait WriterFormatter[A] extends Formatter[A] {
  def writeReports(reports: Seq[BookReport])(implicit writer: PrintWriter): A
  override def apply(env: OutputEnv): A = writeReports(env.reports)(env.out.toWriter)
}
