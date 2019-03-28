package net.paploo.leioparse.formatter

import com.github.tototoshi.csv.CSVWriter
import net.paploo.leioparse.data.core.BookReport

/**
  * Convenience trait to wrap a CSVWriter.
  *
  * Note that while one theoretically should close the CSVWriter, there is no point in doing so, since all it does is
  * close the writer, and its lifecycle is managed somewhere else.
  */
trait CSVFormatter[A] extends Formatter[A] {
  def writeReports(reports: Seq[BookReport])(implicit csv: CSVWriter): A
  override def apply(env: FormatterEnv): A = writeReports(env.reports)(new CSVWriter(env.out.toWriter))
}
