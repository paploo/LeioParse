package net.paploo.leioparse.bookoverlayparser

import java.io.File
import java.nio.file.Path

import cats.Traverse
import cats.data.Kleisli
import cats.implicits._
import net.paploo.leioparse.bookoverlayparser.BookOverlayParser.BookOverlay
import net.paploo.leioparse.bookoverlayparser.JsonBookOverlayParser.RawBookOverlay
import net.paploo.leioparse.data.core.Book
import net.paploo.leioparse.util.quantities.WordDensity
import net.paploo.leioparse.util.extensions.LoggingExtensions.Implicits._
import net.paploo.leioparse.util.functional.Functional
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait BookOverlayParser {
  implicit val logger: Logger = LoggerFactory.getLogger(getClass)
  def parse(implicit ec: ExecutionContext): Future[Seq[BookOverlay]]
}

object BookOverlayParser {

  case class BookOverlay(title: Book.Title,
                         identifier: Option[Book.Id],
                         wordDensity: WordDensity)

  def apply(overlayFilePath: Path): BookOverlayParser = JsonFilePathBookOverlayJsonParser(overlayFilePath)

}

trait JsonBookOverlayParser {
  self: BookOverlayParser =>

  import io.circe.generic.auto._, io.circe.parser._

  def getJson(implicit ec: ExecutionContext): Future[String]

  def parseJson(implicit ec: ExecutionContext): Future[Seq[BookOverlay]] = {
    val convertBooks: List[RawBookOverlay] => Try[List[BookOverlay]] = Functional.swap(Traverse[List].traverse[Try, RawBookOverlay, BookOverlay])(_.toBookOverlay)
    val parseJsonToRawBookOverlaysK = Kleisli((decode[List[RawBookOverlay]] _) andThen (_.toTry))
    val parseJsonToRawBookOverlays: String => Try[List[RawBookOverlay]] = (decode[List[RawBookOverlay]] _) andThen (_.toTry)
    val parseJsonToBookOverlays: String => Try[List[BookOverlay]] = (parseJsonToRawBookOverlaysK andThen convertBooks).run
    val h: String => Try[List[BookOverlay]] = parseJsonToRawBookOverlays andThen (_ flatMap convertBooks)

    getJson.flatMap(json => Future.fromTry(parseJsonToBookOverlays(json)))
  }

    //getJson flatMap (Kleisli((decode[Seq[RawBookOverlay]] _) andThen (_.toTry)) flatMap ()) // andThen Future.fromTry)

}

object JsonBookOverlayParser {

  case class RawBookOverlay(title: String,
                            id: Option[Int],
                            wordDensity: Int) {
    def toBookOverlay: Try[BookOverlay] = Try(BookOverlay(
      title = Book.Title(title),
      identifier = id.map(Book.Id.apply),
      wordDensity = WordDensity(wordDensity)
    ))
  }

}

trait FileBookOverlayParser {
  self: BookOverlayParser =>

  def overlayFile: File

  def readFile(implicit ec: ExecutionContext): Future[String] = Future {
    scala.io.Source.fromFile(overlayFile).getLines().mkString("\n")
  }

}

case class JsonFilePathBookOverlayJsonParser(overlayFilePath: Path) extends BookOverlayParser with FileBookOverlayParser with JsonBookOverlayParser {

  override val overlayFile: File = overlayFilePath.toFile

  override def parse(implicit ec: ExecutionContext): Future[Seq[BookOverlay]] = parseJson

  override def getJson(implicit ec: ExecutionContext): Future[String] = readFile.log(json => s"json = $json")

}
