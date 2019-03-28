package net.paploo.leioparse.util.csv

import java.io.File

import com.github.tototoshi.csv.CSVReader
import net.paploo.leioparse.util.csv.CSVFile.Row

import scala.concurrent.{ExecutionContext, Future, blocking}

case class CSVFile(file: File) {

  def read(implicit ec: ExecutionContext): Future[List[Row]] = readRaw.map(_ map Row.from)

  private[this] def readRaw(implicit ec: ExecutionContext): Future[List[Map[String, String]]] = Future {
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

  case class Row(toMap: Map[Row.Key, Row.Value])

  object Row {
    case class Key(value: String)
    type Value = String

    def from(rawRow: Map[String, String]): Row = Row(rawRow.map { case (k,v) => (Row.Key(k), v)})
  }

}