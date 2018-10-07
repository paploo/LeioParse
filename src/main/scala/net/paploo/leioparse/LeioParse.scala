package net.paploo.leioparse

import java.io.File

import com.github.tototoshi.csv.{CSVReader, CSVWriter}
import net.paploo.leioparse.data.Session
import net.paploo.leioparse.formatter.DefaultSessionFormatter
import net.paploo.leioparse.parser.{BookParser, DateParser, DefaultRowParser, DurationParser, Row}

class LeioParse {

  val rowBuilder: Map[String, String] => Row = Row.fromRaw

  val rowParser: Row => Session = new DefaultRowParser(
    BookParser.mutable(),
    DateParser.standard,
    DurationParser.leio,
    _.toInt
  )

  val sessionFormatter: Session => Seq[String] = new DefaultSessionFormatter()

  val headers: Seq[String] = DefaultSessionFormatter.headers

  def run(args: Seq[String]): Unit = {
    //val filePath = "/Users/paploo/Dropbox/Leio/leio_09-29-18/leio_sessions.csv"
    System.err.println(s"args = $args")
    val filePath = args.head

    val file: File = new File(filePath)

    val reader = CSVReader.open(file)
    val formattedRows = reader.iteratorWithHeaders.map(rowBuilder andThen rowParser andThen sessionFormatter).toStream

    val writer = CSVWriter.open(new java.io.OutputStreamWriter(System.out))
    writer.writeRow(headers)
    writer.writeAll(formattedRows)

    reader.close()
    writer.close()
  }

}

object LeioParse {

  def main(args: Array[String]): Unit = {
    new LeioParse().run(args)
  }

}