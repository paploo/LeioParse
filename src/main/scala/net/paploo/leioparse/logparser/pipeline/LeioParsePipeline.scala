package net.paploo.leioparse.logparser.pipeline

import java.nio.file.Path

import cats.Functor
import net.paploo.leioparse.logparser.pipeline.LeioParsePipeline.DataDirectory
import net.paploo.leioparse.util.extensions.Implicits._

trait LeioParsePipeline[A] extends (DataDirectory => Seq[A])

object LeioParsePipeline {

  case class DataDirectory(path: Path)

  case class DataFile(path: Path)

  def apply[A](fileLocator: LeioFileLocator, reader: LeioReader, parser: LeioParser[A]): LeioParsePipeline[A] =
    dataDirectory => ((fileLocator andThen reader) andThen Functor[Seq].lift(parser))(dataDirectory)

}