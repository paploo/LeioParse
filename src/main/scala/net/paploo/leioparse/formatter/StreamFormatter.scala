package net.paploo.leioparse.formatter

import java.io.OutputStream

import net.paploo.leioparse.data.core.BookReport

/**
  * Convenience trait for building binary formatters with an implicit OutputStream.
  */
trait StreamFormatter[A] extends Formatter[A] {
  def writeReports(reports: Seq[BookReport])(implicit stream: OutputStream): A
  override def apply(env: FormatterEnv): A = writeReports(env.reports)(env.out.toOutputStream)
}
