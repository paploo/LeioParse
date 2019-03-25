package net.paploo.leioparse

import java.nio.file.Paths
import java.util.concurrent.TimeUnit

import cats.implicits._
import net.paploo.leioparse.app.{App, AppArgs, StandardApp, TestApps}
import net.paploo.leioparse.app.App.Result.ShowResult
import net.paploo.leioparse.util.extensions.Implicits._
import net.paploo.leioparse.util.extensions.LoggingExtensions.Logging

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

object LeioParse extends Logging {

  implicit val appExecutionContext: ExecutionContext = ExecutionContext.global

  def main(implicit rawArgs: Array[String]): Unit = {
    (for {
      config <- getConfig
      app <- getApp
      appArgs <- getAppArgs
      appTimeout = Duration(config.timeoutSeconds, TimeUnit.SECONDS)
      runResult <- app.run(appArgs).toTry(appTimeout)
    } yield runResult) match {
      case Success(result) =>
        result.log(r => s"LeioParse.main(${rawArgs.toList}) --> ${r.show}")
        System.exit(0)
      case Failure(throwable) =>
        throwable.log(th => s"LeioParse encountered a fatal error: $th")
        throw throwable
    }
  }

  def getConfig(implicit rawArgs: Array[String]): Try[LeioParseConfig] = Try(LeioParseConfig(timeoutSeconds = 60))

  def getApp(implicit rawArgs: Array[String]): Try[App[_]] =
    Try(StandardApp)
//    Try(TestApps.ParseOverlayAndLogApp andThenRun TestApps.ParseLeioFilesAndLogApp)
//    Try(TestApps.ParseLeioFilesAndLogApp)

  def getAppArgs(implicit rawArgs: Array[String]): Try[AppArgs] = Try(AppArgs(
    dataDirPath = Paths.get(rawArgs.head),
    bookOverlayPath = Paths.get("./books.json")
  )).recoverWith {
    case th => Failure(new IllegalArgumentException(s"Could not parse application arguments", th))
  }

}