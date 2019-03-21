package net.paploo.leioparse.logparser.pipeline

import java.nio.file.Paths

import net.paploo.leioparse.logparser.pipeline.LeioParsePipeline.{DataDirectory, DataFile}

trait LeioFileLocator extends (DataDirectory => DataFile)

object LeioFileLocator {

  val bookFileLocator: LeioFileLocator = dataDirectory => DataFile(Paths.get(dataDirectory.path.toString, "leio_sessions.csv"))

  val sessionFileLocator: LeioFileLocator = dataDirectory => DataFile(Paths.get(dataDirectory.path.toString, "leio_sessions.csv"))

}
