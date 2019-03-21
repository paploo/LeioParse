package net.paploo.leioparse.logparser

import net.paploo.leioparse.logparser.data.{LeioBook, LeioSession}
import net.paploo.leioparse.logparser.pipeline.LeioParsePipeline
import net.paploo.leioparse.logparser.pipeline.LeioParsePipeline.DataDirectory

trait LeioLogParser {
  def bookPipeline: LeioParsePipeline[Seq[LeioBook]]
  def sessionPipeline: LeioParsePipeline[Seq[LeioSession]]
}

object LeioLogParser {

  def standardV2(dataDiretory: DataDirectory): LeioLogParser = ???

  def apply(bookPipe: LeioParsePipeline[Seq[LeioBook]], sessionPipe: LeioParsePipeline[Seq[LeioSession]]): LeioLogParser = new LeioLogParser {
    override def bookPipeline: LeioParsePipeline[Seq[LeioBook]] = bookPipe
    override def sessionPipeline: LeioParsePipeline[Seq[LeioSession]] = sessionPipe
  }

}







