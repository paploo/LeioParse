package net.paploo.leioparse.logparser.pipeline

import java.nio.file.Path

import cats.Functor
import cats.implicits._
import net.paploo.leioparse.logparser.pipeline.LeioParsePipeline.DataDirectory
import net.paploo.leioparse.util.extensions.Implicits._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

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
    val rowF: DataDirectory => Future[Seq[Row]] = fileLocator andThen reader
    val parserF: Row => Try[A] = parser
    val f1: Row => Future[A] = row => Future.fromTry(parser(row))
    val f2: Seq[Row] => Future[Seq[A]] = rows => Future.sequence(rows.map(f1))
    dataDirectory => rowF(dataDirectory) flatMap f2
  }

  private[this] def sequenceTry[A](seq: Seq[Try[A]]): Try[Seq[A]] = seq.foldLeft[Try[Seq[A]]](Success(Vector.empty[A])){
    case (ts, Success(a)) => ts.map(_ :+ a)
    case (ts, Failure(e)) => Failure(e)
  }

}