package net.paploo.leioparse

import java.nio.file.Paths
import java.util.concurrent.TimeUnit

import net.paploo.leioparse.app.{App, AppArgs, _}
import net.paploo.leioparse.util.extensions.Implicits._
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

object LeioParse {

  implicit val logger: Logger = LoggerFactory.getLogger(getClass)

  def main(implicit rawArgs: Array[String]): Unit = {
    (for {
      config <- getConfig
      app <- getApp
      appArgs <- getAppArgs
      appTimeout = Duration(config.timeoutSeconds, TimeUnit.SECONDS)
      runResult <- app.run(appArgs).toTry(appTimeout)
    } yield runResult) match {
      case Success(result) =>
        result.log(r => s"LeioParse.main($rawArgs) --> \n\t $r")
        System.exit(0)
      case Failure(throwable) =>
        throwable.log(th => s"LeioParse encountered a fatal error: $th")
        throw throwable
    }
  }

  def getConfig(implicit rawArgs: Array[String]): Try[LeioParseConfig] = Try(LeioParseConfig(timeoutSeconds = 60))

  def getApp(implicit rawArgs: Array[String]): Try[App] =
    Try(ParseOverlayAndLogApp)
  //Try(ParseLeioFilesAndLogApp)

  def getAppArgs(implicit rawArgs: Array[String]): Try[AppArgs] = Try(AppArgs(
    Paths.get(rawArgs.head)
  )).recoverWith {
    case th => Failure(new IllegalArgumentException(s"Could not parse application arguments", th))
  }

}