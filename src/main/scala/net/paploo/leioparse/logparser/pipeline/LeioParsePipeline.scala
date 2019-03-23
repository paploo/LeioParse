package net.paploo.leioparse.logparser.pipeline

import java.nio.file.Path

import cats.Traverse
import cats.data.Kleisli
import cats.implicits._
import net.paploo.leioparse.logparser.data.{LeioBook, LeioSession}
import net.paploo.leioparse.logparser.pipeline.LeioParsePipeline.DataDirectory
import net.paploo.leioparse.util.functional.Functional

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

  def apply[A](fileLocator: LeioFileLocator, reader: LeioReader, parser: LeioParser[A])(implicit ec: ExecutionContext): LeioParsePipeline[A] = {
    val rowsParser: List[Row] => Future[List[A]] = Functional.swap(Traverse[List].traverse[Try, Row, A])(parser) andThen Future.fromTry
    dataDirectory => (Kleisli(fileLocator andThen reader) andThen rowsParser).run(dataDirectory)
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