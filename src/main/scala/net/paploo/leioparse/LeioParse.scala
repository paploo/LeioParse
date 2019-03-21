package net.paploo.leioparse

import java.nio.file.Paths
import java.util.concurrent.TimeUnit

import net.paploo.leioparse.app.{App, AppArgs}
import net.paploo.leioparse.util.extensions.Implicits._

import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

object LeioParse {

  def main(args: Array[String]): Unit = {
    val config = getConfig(args)
    val appArgs = getAppArgs(args)
    val appTimeout = Duration(config.timeoutSeconds, TimeUnit.SECONDS)

    App.apply(appArgs).toTry(appTimeout) match {
      case Success(_) => System.exit(0)
      case Failure(th) => throw th
    }
  }

  def getConfig(args: Array[String]): LeioParseConfig = LeioParseConfig(timeoutSeconds = 60)

  def getAppArgs(args: Array[String]): AppArgs = AppArgs(Paths.get(args.head))

}