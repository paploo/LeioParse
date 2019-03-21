package net.paploo.leioparse

import java.nio.file.Paths
import java.util.concurrent.TimeUnit

import net.paploo.leioparse.app.{App, AppArgs, AppConfig}
import net.paploo.leioparse.util.extensions.Implicits._

import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

object LeioParse {

  def main(args: Array[String]): Unit = {
    val appConfig = getAppConfig(args)
    val appArgs = getAppArgs(args)
    val appTimeout = getAppTimeout(args)

    App(appConfig)(appArgs).toTry(appTimeout) match {
      case Success(_) => System.exit(0)
      case Failure(th) => throw th
    }
  }

  def getAppConfig(args: Array[String]): AppConfig = AppConfig(timeoutSeconds = 60)

  def getAppArgs(args: Array[String]): AppArgs = AppArgs(Paths.get(args.head))

  def getAppTimeout(args: Array[String]): Duration = Duration(getAppConfig(args).timeoutSeconds, TimeUnit.SECONDS)

}