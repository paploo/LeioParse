package net.paploo.leioparse.leiologparser.pipeline

import net.paploo.leioparse.leiologparser.pipeline.LeioParsePipeline.{DataFile, Row}
import net.paploo.leioparse.util.csv.CSVFile

import scala.concurrent.{ExecutionContext, Future}

trait LeioReader extends (DataFile => Future[List[Row]])

object LeioReader {

  def csvParser(implicit ec: ExecutionContext): LeioReader = CSVFileLeioReader.apply

  private[LeioReader] class CSVFileLeioReader(implicit val ec: ExecutionContext) extends LeioReader {

    override def apply(dataFile: DataFile): Future[List[Row]] = read(dataFile).map {
      _.map(convertCSVRow)
    }

    private[this] def read(dataFile: DataFile): Future[List[CSVFile.Row]] = CSVFile(dataFile.path.toFile).read

    private[this] def convertCSVRow(csvRow: CSVFile.Row): Row = Row(csvRow.toMap.map {
      case (CSVFile.Row.Key(key), value) => (Row.Key(key), if (value.nonEmpty) Some(Row.Value(value)) else None)
    })

  }

 private[LeioReader] object CSVFileLeioReader {
    def apply(implicit ec: ExecutionContext): CSVFileLeioReader = new CSVFileLeioReader
  }

}
