package net.paploo.leioparse.leiologparser.pipeline

import java.nio.file.Path

import cats.Traverse
import cats.data.Kleisli
import cats.implicits._
import net.paploo.leioparse.data.leiofile.{LeioBook, LeioSession}
import net.paploo.leioparse.leiologparser.pipeline.LeioParsePipeline.DataDirectory
import net.paploo.leioparse.util.functional.Functional._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait LeioParsePipeline[A] extends (DataDirectory => Future[Seq[A]])

object LeioParsePipeline {

  type LeioParsePipelineBuilder[A] = ExecutionContext => LeioParsePipeline[A]

  case class DataDirectory(path: Path)

  case class DataFile(path: Path)

  case class Row(toMap: Map[Row.Key, Option[Row.Value]]) {
    def get(key: Row.Key): Option[Row.Value] = toMap.get(key).flatten
  }

  object Row {
    case class Key(value: String)
    case class Value(value: String)
  }

  case class LeioParsePipelineException(message: String, cause: Throwable) extends RuntimeException(message, cause)

  def apply[A](fileLocator: LeioFileLocator, reader: LeioReader, parser: LeioParser[A])(implicit ec: ExecutionContext): LeioParsePipeline[A] = {
    val rowsParser: List[Row] => Future[List[A]] = swap(Traverse[List].traverse[Try, Row, A])(parser) andThen Future.fromTry
    dataDirectory => (Kleisli(fileLocator andThen reader) andThen rowsParser).run(dataDirectory) recoverWith {
      case th => Future.failed(LeioParsePipelineException(s"Unable to process Leio log file with cause: $th", th))
    }
  }

  def leioBookPipeline(implicit ec: ExecutionContext): LeioParsePipeline[LeioBook] = apply(
    LeioFileLocator.BookFileLocator,
    LeioReader.csvParser,
    LeioParser.BookParser
  )

  def leioBookPipelineBuilder: LeioParsePipelineBuilder[LeioBook] = leioBookPipeline(_)

  def leioSessionPipeline(implicit ec: ExecutionContext): LeioParsePipeline[LeioSession] = apply(
    LeioFileLocator.SessionFileLocator,
    LeioReader.csvParser,
    LeioParser.SessionParser
  )

  def leioSessionPipelineBuilder: LeioParsePipelineBuilder[LeioSession] = leioSessionPipeline(_)

}