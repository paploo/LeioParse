package net.paploo.leioparse.logparser.pipeline

import net.paploo.leioparse.logparser.pipeline.LeioParsePipeline.DataFile
import net.paploo.leioparse.util.csv.CSVFile

trait LeioReader extends (DataFile => Seq[CSVFile.Row])

object LeioReader {
}
