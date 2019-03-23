package net.paploo.leioparse.leiologparser

import java.nio.file.Path

import net.paploo.leioparse.leiologparser.data.{LeioBook, LeioSession}
import net.paploo.leioparse.leiologparser.pipeline.LeioParsePipeline
import net.paploo.leioparse.leiologparser.pipeline.LeioParsePipeline.{DataDirectory, LeioParsePipelineBuilder}

import scala.concurrent.{ExecutionContext, Future}

trait LeioLogParser {
  def parseBooks(implicit ec: ExecutionContext): Future[Seq[LeioBook]]
  def parseSessions(implicit ec: ExecutionContext): Future[Seq[LeioSession]]
}

object LeioLogParser {

  def forPath(path: Path): LeioLogParser = fromDataDirectory(DataDirectory(path))

  def fromDataDirectory(dir: DataDirectory): LeioLogParser =
    fromPipelineBuilders(LeioParsePipeline.leioBookPipelineBuilder, LeioParsePipeline.leioSessionPipelineBuilder)(dir)

  def fromPipelineBuilders(bookPipelineBuilder: LeioParsePipelineBuilder[LeioBook], sessionPipelineBuilder: LeioParsePipelineBuilder[LeioSession])(dir: DataDirectory): LeioLogParser =
    LeioLogPipelineBuilderParser.apply(dir, bookPipelineBuilder, sessionPipelineBuilder)

}

trait LeioLogPipelinedParser extends LeioLogParser {
  def dataDirectory: DataDirectory

  def bookPipeline(implicit ec: ExecutionContext): LeioParsePipeline[LeioBook]
  def sessionPipeline(implicit ec: ExecutionContext): LeioParsePipeline[LeioSession]

  override def parseBooks(implicit ec: ExecutionContext): Future[Seq[LeioBook]] = bookPipeline.apply(dataDirectory)
  override def parseSessions(implicit ec: ExecutionContext): Future[Seq[LeioSession]] = sessionPipeline.apply(dataDirectory)
}

class LeioLogPipelineBuilderParser(val dataDirectory: DataDirectory)
                                  (bookPipelineBuilder: LeioParsePipelineBuilder[LeioBook], sessionPipelineBuilder: LeioParsePipelineBuilder[LeioSession]) extends LeioLogPipelinedParser {
  override def bookPipeline(implicit ec: ExecutionContext): LeioParsePipeline[LeioBook] = bookPipelineBuilder(ec)
  override def sessionPipeline(implicit ec: ExecutionContext): LeioParsePipeline[LeioSession] = sessionPipelineBuilder(ec)
}

object LeioLogPipelineBuilderParser {
  def apply(dataDirectory: DataDirectory,
            bookPipelineBuilder: LeioParsePipelineBuilder[LeioBook],
            sessionPipelineBuilder: LeioParsePipelineBuilder[LeioSession]): LeioLogPipelineBuilderParser =
    new LeioLogPipelineBuilderParser(dataDirectory)(bookPipelineBuilder, sessionPipelineBuilder)
}







