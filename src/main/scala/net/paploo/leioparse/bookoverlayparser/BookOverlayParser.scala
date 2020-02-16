package net.paploo.leioparse.bookoverlayparser

import java.io.File
import java.nio.file.Path

import cats.Traverse
import cats.data.Kleisli
import cats.implicits._
import net.paploo.leioparse.bookoverlayparser.BookOverlayParser.BookOverlayParserException
import net.paploo.leioparse.data.overlay.BookOverlay
import net.paploo.leioparse.bookoverlayparser.JsonBookOverlayParser.RawBookOverlay
import net.paploo.leioparse.data.core.Book
import net.paploo.leioparse.util.quantities.WordDensity
import net.paploo.leioparse.util.extensions.LoggingExtensions.Implicits._
import net.paploo.leioparse.util.extensions.LoggingExtensions.Logging
import net.paploo.leioparse.util.functional.Functional._

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Codec
import scala.util.Try

trait BookOverlayParser extends Logging {
  def parse(implicit ec: ExecutionContext): Future[Seq[BookOverlay]]
}

object BookOverlayParser {

  case class BookOverlayParserException(message: String, cause: Throwable) extends RuntimeException(message, cause)

  def apply(overlayFilePath: Path): BookOverlayParser = JsonFilePathBookOverlayJsonParser(overlayFilePath)

}

trait JsonBookOverlayParser {
  self: BookOverlayParser =>

  import io.circe.generic.auto._, io.circe.parser._

  def getJson(implicit ec: ExecutionContext): Future[String]

  def parseJson(implicit ec: ExecutionContext): Future[Seq[BookOverlay]] = {
    val convertBooks: List[RawBookOverlay] => Try[List[BookOverlay]] = swap(Traverse[List].traverse[Try, RawBookOverlay, BookOverlay])(_.toBookOverlay)
    val parseJsonToRawBookOverlays: String => Try[List[RawBookOverlay]] = (decode[List[RawBookOverlay]] _) andThen (_.toTry)
    val parseJsonToBookOverlays: String => Try[List[BookOverlay]] = (Kleisli(parseJsonToRawBookOverlays) andThen convertBooks).run
    getJson flatMap (parseJsonToBookOverlays andThen Future.fromTry)
  } recoverWith {
    case th => Future.failed(BookOverlayParserException(s"Unable to parse overlay json with cause $th", th))
  }

}

object JsonBookOverlayParser {

  case class RawBookOverlay(title: String,
                            id: Option[String],
                            wordDensity: Option[Double]) {
    def toBookOverlay: Try[BookOverlay] = Try(BookOverlay(
      title = Book.Title(title),
      externalId = id.map(Book.ExternalId.apply),
      wordDensity = wordDensity.map(WordDensity.apply),
    ))
  }

}

trait FileBookOverlayParser {
  self: BookOverlayParser =>

  def overlayFile: File

  def readFile(implicit ec: ExecutionContext): Future[String] = Future {
    scala.io.Source.fromFile(overlayFile)(Codec.UTF8).getLines().mkString("\n")
  }

}

case class JsonFilePathBookOverlayJsonParser(overlayFilePath: Path) extends BookOverlayParser with FileBookOverlayParser with JsonBookOverlayParser {

  override val overlayFile: File = overlayFilePath.toFile

  override def parse(implicit ec: ExecutionContext): Future[Seq[BookOverlay]] = parseJson

  override def getJson(implicit ec: ExecutionContext): Future[String] = readFile.log(json => s"overlayJson = $json")

}
