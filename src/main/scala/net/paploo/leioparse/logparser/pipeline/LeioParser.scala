package net.paploo.leioparse.logparser.pipeline

import net.paploo.leioparse.util.csv.CSVFile

trait LeioParser[A] extends (CSVFile.Row => A)

object LeioParser {
}