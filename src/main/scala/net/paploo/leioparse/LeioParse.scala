package net.paploo.leioparse

import java.nio.file.Paths
import java.util.concurrent.TimeUnit

import net.paploo.leioparse.app.{App, AppArgs}
import net.paploo.leioparse.util.extensions.Implicits._

import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

object LeioParse {

  def main(args: Array[String]): Unit = {
    (for {
      config <- getConfig(args)
      appArgs <- getAppArgs(args)
      appTimeout = Duration(config.timeoutSeconds, TimeUnit.SECONDS)
      runResult <- App.apply(appArgs).toTry(appTimeout)
    } yield runResult) match {
      case Success(_) => System.exit(0)
      case Failure(th) => throw th
    }
  }

  def getConfig(args: Array[String]): Try[LeioParseConfig] = Try(LeioParseConfig(timeoutSeconds = 60))

  def getAppArgs(args: Array[String]): Try[AppArgs] = Try(AppArgs(
    Paths.get(args.head)
  )).recoverWith {
    case th => Failure(new IllegalArgumentException(s"Could not parse application arguments", th))
  }

}