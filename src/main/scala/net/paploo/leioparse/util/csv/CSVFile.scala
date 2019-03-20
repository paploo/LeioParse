package net.paploo.leioparse.util.csv

import java.io.File

import com.github.tototoshi.csv.CSVReader
import net.paploo.leioparse.util.csv.CSVFile.{ColumnHeader, ColumnValue, Row, RowParser}

import scala.concurrent.{ExecutionContext, Future, blocking}

case class CSVFile(file: File) {

  def read(implicit ec: ExecutionContext): Future[Seq[Row]] = readRaw.map(_ map Row.apply)

  def parse[A](parser: RowParser[A])(implicit ec: ExecutionContext): Future[Seq[A]] = read.map(_ map parser)

  private[this] def readRaw(implicit ec: ExecutionContext): Future[Seq[Map[ColumnHeader, ColumnValue]]] = Future {
    blocking {
      val reader = CSVReader.open(file)
      try {
        //Since the iterator is lazy, we convert to a concrete Seq using `toList`, which fetches everything now.
        //If we did not, then the CSVReader would close before we can do anything with the data.
        reader.iteratorWithHeaders.toList
      } finally {
        reader.close()
      }
    }
  }

}

object CSVFile {

  type ColumnHeader = String
  type ColumnValue = String

  case class Row(toMap: Map[ColumnHeader, ColumnValue])

  type RowParser[A] = Row => A

}