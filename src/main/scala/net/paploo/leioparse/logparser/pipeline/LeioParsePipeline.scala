package net.paploo.leioparse.logparser.pipeline

import java.nio.file.Path

import cats.Traverse
import cats.data.Kleisli
import cats.implicits._
import net.paploo.leioparse.logparser.pipeline.LeioParsePipeline.DataDirectory

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

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
    val rowsParser1: List[Row] => Future[List[A]] = rows => Future.sequence(rows.map(row => Future.fromTry(parser(row))))
    val rowsParser: List[Row] => Future[List[A]] = swap(Traverse[List].traverse[Try, Row, A])(parser) andThen Future.fromTry

    val f = (Kleisli(fileLocator andThen reader) andThen (swap(Traverse[List].traverse[Try, Row, A])(parser) andThen Future.fromTry)).run

    dataDirectory => (Kleisli(fileLocator andThen reader) andThen rowsParser).run(dataDirectory)
  }

  //TODO: Move into standard library
  private[this] def swap[A, B, C](f: A => B => C): B => A => C = b => a => f(a)(b)

}