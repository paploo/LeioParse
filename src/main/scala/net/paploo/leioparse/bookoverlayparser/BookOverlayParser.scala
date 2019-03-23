package net.paploo.leioparse.bookoverlayparser

import java.io.File
import java.nio.file.Path

import net.paploo.leioparse.bookoverlayparser.BookOverlayParser.BookOverlay
import net.paploo.leioparse.data.core.Book
import net.paploo.leioparse.util.quantities.WordDensity

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait BookOverlayParser {
  def parse(implicit ec: ExecutionContext): Future[Seq[BookOverlay]]
}

object BookOverlayParser {

  case class BookOverlay(title: Book.Title,
                         identifier: Option[Int],
                         wordDensity: WordDensity)

}

class CoreBookOverlayJsonParser(jsonFilePath: Path) extends BookOverlayParser {

  import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

  override def parse(implicit ec: ExecutionContext): Future[Seq[BookOverlay]] = for {
    json <- readFile(jsonFilePath.toFile)
    result <- Future.fromTry(parseJson(json))
  } yield result

  private[this] def readFile(file: File)(implicit ec: ExecutionContext): Future[String] = Future {
    scala.io.Source.fromFile(file).getLines().mkString("\n")
  }

  private[this] def parseJson(json: String): Try[Seq[BookOverlay]] = decode[Seq[BookOverlay]](json).toTry

}
