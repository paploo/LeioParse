package net.paploo.leioparse

import java.io.{File, OutputStreamWriter}
import java.nio.file.{Path, Paths}

import com.github.tototoshi.csv.{CSVReader, CSVWriter}
import net.paploo.leioparse.data.{Book, BookLibrary, Session}
import net.paploo.leioparse.formatter.DefaultSessionFormatter
import net.paploo.leioparse.parser.book.{BookRow, DefaultBookRowParser}
import net.paploo.leioparse.parser.session.{DefaultSessionRowParser, SessionRow}
import net.paploo.leioparse.parser.{DateParser, DurationParser}

class LeioParse {

  val bookRowBuilder: Map[String, String] => BookRow = BookRow.fromRaw

  val bookRowParser: BookRow => Book.Data = new DefaultBookRowParser(identity,
                                                                     identity)

  val bookLibraryBuilder: Seq[Book.Data] => BookLibrary = BookLibrary.build(BookLibrary.IdGenerator.byIndex)

  val sessionRowBuilder: Map[String, String] => SessionRow = SessionRow.fromRaw

  def sessionRowParser(bookParser: String => Book): SessionRow => Session = new DefaultSessionRowParser(
    bookParser,
    DateParser.standard,
    DurationParser.leio,
    _.toInt
  )

  val sessionFormatter: Session => Seq[String] = new DefaultSessionFormatter()

  val headers: Seq[String] = DefaultSessionFormatter.headers

  def run(args: Seq[String]): Unit = {
    System.err.println(s"args = $args")
    val dataDirPath: Path = Paths.get(args.head)
    val sessionPath: Path = Paths.get(dataDirPath.toString, "leio_sessions.csv")
    val bookPath: Path = Paths.get(dataDirPath.toString, "leio_data.csv")

    val bookLibrary: BookLibrary = readBookLibrary(bookPath.toFile)

    val formattedRows = readFormattedRows(bookLibrary)(sessionPath.toFile)

    outputFormattedRows(new OutputStreamWriter(System.out))(formattedRows)
  }

  private [this] def readBookLibrary(bookFile: File): BookLibrary = {
    val bookReader = CSVReader.open(bookFile)
    try {
      bookLibraryBuilder(bookReader.iteratorWithHeaders.map(bookRowBuilder andThen bookRowParser).toSeq)
    } finally {
      bookReader.close()
    }
  }

  private[this] def readFormattedRows(bookLibrary: BookLibrary)(sessionFile: File): Seq[Seq[String]] = {
    val sessionReader = CSVReader.open(sessionFile)
    val bookGetter: String => Book = name => bookLibrary.findByName(name).getOrElse(Book.unknown)
    try {
      //NOTE: I have a choice, leave as a stream, but then don't close the session reader until full processing, *or* realize explicitly in memory as a list so that I can close it eagerly.
      sessionReader.iteratorWithHeaders.map(sessionRowBuilder andThen sessionRowParser(bookGetter) andThen sessionFormatter).toList
    } finally {
      sessionReader.close
    }
  }

  private[this] def outputFormattedRows(outputWriter: OutputStreamWriter)(formattedRows: Seq[Seq[String]]): Seq[Seq[String]] = {
    val writer = CSVWriter.open(outputWriter)
    try {
      writer.writeRow(headers)
      writer.writeAll(formattedRows)
      formattedRows
    } finally {
      writer.close()
    }
  }

}

object LeioParse {

  def main(args: Array[String]): Unit = {
    new LeioParse().run(args)
  }

}