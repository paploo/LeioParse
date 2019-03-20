package net.paploo.leioparse

import java.io.{File, OutputStreamWriter}
import java.nio.file.{Path, Paths}
import java.time.LocalDateTime

import com.github.tototoshi.csv.{CSVReader, CSVWriter}
import net.paploo.leioparse.data.{Book, BookLibrary, Session}
import net.paploo.leioparse.formatter.DefaultSessionFormatter
import net.paploo.leioparse.parser.book.{BookRow, BookRowOverlayData, DefaultBookRowParser}
import net.paploo.leioparse.parser.session.{DefaultSessionRowParser, SessionRow}
import net.paploo.leioparse.parser.{DateParser, DurationParser}

class LeioParse {

  val bookRowBuilder: Map[String, String] => BookRow = BookRow.fromRaw

  def bookRowParser(bookRowOverlayData: Book.Title => Option[BookRowOverlayData]): BookRow => Book = new DefaultBookRowParser(
    identity,
    identity,
    bookRowOverlayData
  )

  val bookLibraryBuilder: Seq[Book] => BookLibrary = BookLibrary.apply

  val sessionRowBuilder: Map[String, String] => SessionRow = SessionRow.fromRaw

  def sessionRowParser(bookParser: Book.Title => Book): SessionRow => Session = new DefaultSessionRowParser(
    (Book.Title.apply _).andThen(bookParser),
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

    val bookRowOverlayData:  Map[Book.Title, BookRowOverlayData] = readBookRowOverlayData(null) //TODO: Load from file

    val bookLibrary: BookLibrary = readBookLibrary(bookRowOverlayData.get)(bookPath.toFile)

    val formattedRows = readFormattedRows(bookLibrary)(sessionPath.toFile)

    outputFormattedRows(new OutputStreamWriter(System.out))(formattedRows)
  }

  private[this] def readBookRowOverlayData(bookWordsFile: File): Map[Book.Title, BookRowOverlayData] =
    LeioParse.bookRowOverlayData //TODO: Load this from a file.

  private [this] def readBookLibrary(bookRowOverlayData: Book.Title => Option[BookRowOverlayData])(bookFile: File): BookLibrary = {
    val bookReader = CSVReader.open(bookFile)
    try {
      //NOTE: I have a choice, leave as a stream, but then don't close the session reader until full processing, *or* realize explicitly in memory as a list so that I can close it eagerly.
      bookLibraryBuilder(bookReader.iteratorWithHeaders.map(bookRowBuilder andThen bookRowParser(bookRowOverlayData)).toList)
    } finally {
      bookReader.close()
    }
  }

  private[this] def readFormattedRows(bookLibrary: BookLibrary)(sessionFile: File): Seq[Seq[String]] = {
    import LeioParse.LocalDateTimeOrdering
    val sessionReader = CSVReader.open(sessionFile)
    val bookGetter: Book.Title => Book = title => bookLibrary.findByTitle(title).getOrElse(Book.unknown)
    try {
      //NOTE: I have a choice, leave as a stream, but then don't close the session reader until full processing, *or* realize explicitly in memory as a list so that I can close it eagerly.
      sessionReader.iteratorWithHeaders.map(
        sessionRowBuilder andThen sessionRowParser(bookGetter)
      ).toList.sortBy(_.date).map(sessionFormatter)
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

  implicit val LocalDateTimeOrdering: Ordering[LocalDateTime] = new Ordering[LocalDateTime] {
    override def compare(x: LocalDateTime, y: LocalDateTime): Int = x.compareTo(y)
  }

  // Used https://www.onlineocr.net to interpret screenshots and https://wordcounter.net to count
  // Chose average looking pages
  // Dune pagesToWordsAndChars = (19,45,71) --> ((426,2503), (436,2410), (458,???)
  // Gardens of the Moon = pages (41,35,47) --> (((500,2891), (555,3076), (577,3218))


  @deprecated(s"Do not use hardcoded values, instead load from file")
  val bookRowOverlayData: Map[Book.Title, BookRowOverlayData] = Map(
    Book.Title("Relic Worlds 1")      -> BookRowOverlayData(1, Some(333)), //Just use the Relic Worlds 2 counts for the moment
    Book.Title("Relic Worlds 2")      -> BookRowOverlayData(2, Some((326+371+303)/3)), //Counts from a few typical looking pages
    Book.Title("Dune")                -> BookRowOverlayData(3, Some((426+436+458)/3)), //Counts from a few typical looking pages
    Book.Title("Gardens of the Moon") -> BookRowOverlayData(4, Some((500+555+577)/3)),
    Book.Title("Norse Mythology")     -> BookRowOverlayData(5, Some((255+261+277)/3)),
    Book.Title("Mortal Engines")      -> BookRowOverlayData(6, Some((283+294+299)/3)),
    Book.Title("Predator's Gold")     -> BookRowOverlayData(7, Some(292)), //Just use the first book's count since same layout.
    Book.Title("Infernal Devices")    -> BookRowOverlayData(8, Some(292)), //Just use the first book's count since same layout.
    Book.Title("A Darkling Plain")    -> BookRowOverlayData(9, Some(292)), //Just use the first books' count since same layout.
  )

}
