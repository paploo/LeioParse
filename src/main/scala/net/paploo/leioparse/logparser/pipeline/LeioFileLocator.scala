package net.paploo.leioparse.logparser.pipeline

import java.nio.file.Paths

import net.paploo.leioparse.logparser.pipeline.LeioParsePipeline.{DataDirectory, DataFile}

trait LeioFileLocator extends (DataDirectory => DataFile)

object LeioFileLocator {

  val BookFileLocator: LeioFileLocator = dataDirectory => DataFile(Paths.get(dataDirectory.path.toString, "leio_sessions.csv"))

  val SessionFileLocator: LeioFileLocator = dataDirectory => DataFile(Paths.get(dataDirectory.path.toString, "leio_sessions.csv"))

}
