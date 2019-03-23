package net.paploo.leioparse

import java.nio.file.Paths
import java.util.concurrent.TimeUnit

import net.paploo.leioparse.app.{App, AppArgs, TestingApp}
import net.paploo.leioparse.util.extensions.Implicits._

import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

object LeioParse {

  def main(implicit rawArgs: Array[String]): Unit = {
    (for {
      config <- getConfig
      app <- getApp
      appArgs <- getAppArgs
      appTimeout = Duration(config.timeoutSeconds, TimeUnit.SECONDS)
      runResult <- app.run(appArgs).toTry(appTimeout)
    } yield runResult) match {
      case Success(_) => System.exit(0)
      case Failure(th) => throw th
    }
  }

  def getConfig(implicit rawArgs: Array[String]): Try[LeioParseConfig] = Try(LeioParseConfig(timeoutSeconds = 60))

  def getApp(implicit rawArgs: Array[String]): Try[App] = Try(new TestingApp)

  def getAppArgs(implicit rawArgs: Array[String]): Try[AppArgs] = Try(AppArgs(
    Paths.get(rawArgs.head)
  )).recoverWith {
    case th => Failure(new IllegalArgumentException(s"Could not parse application arguments", th))
  }

}