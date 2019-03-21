package net.paploo.leioparse.logparser.pipeline

import java.nio.file.Path

import cats.data.Kleisli
import cats.implicits._
import net.paploo.leioparse.logparser.pipeline.LeioParsePipeline.DataDirectory

import scala.concurrent.{ExecutionContext, Future}

trait LeioParsePipeline[A] extends (DataDirectory => Future[Seq[A]])

object LeioParsePipeline {

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
    val rowsParser: Seq[Row] => Future[Seq[A]] = rows => Future.sequence(rows.map(row => Future.fromTry(parser(row))))
    dataDirectory => (Kleisli(fileLocator andThen reader) andThen Kleisli(rowsParser)).run(dataDirectory)
  }

}