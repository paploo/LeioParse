package net.paploo.leioparse

import java.io.File
import java.util.concurrent.TimeUnit

import net.paploo.leioparse.app.AppArgs.FormatterArg
import net.paploo.leioparse.app.{App, AppArgs, StandardApp}
import net.paploo.leioparse.util.extensions.Implicits._
import net.paploo.leioparse.util.extensions.LoggingExtensions.Logging
import scopt.OParser

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

/**
  * The outter shell of the CLI, whose job it is to broker interactions with the outside world.
  *
  * Specifically, it is tasked with interpreting the command line arguments into canonical form,
  * selecting an `App` instance to run, and handling the return value of the app (which could
  * be an exception).
  */
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
        result.log(r => s"LeioParse.main(${rawArgs.toList}) --> ${r.toSeq.mkString(s"Result(\n\t", ",\n\t", "\n)")}")
        System.exit(0)
      case Failure(th @ ArgumentParseException) =>
        th.log(identity)
        System.exit(1)
      case Failure(throwable) =>
        throwable.log(th => s"LeioParse encountered a fatal error: $th")
        throw throwable
    }
  }

  private[this] def getConfig(implicit rawArgs: Array[String]): Try[LeioParseConfig] = Try(LeioParseConfig(timeoutSeconds = 60))

  private[this] def getApp(implicit rawArgs: Array[String]): Try[App[_]] =
    Try(StandardApp)

  private[this] def getAppArgs(implicit rawArgs: Array[String]): Try[AppArgs] =
    OParser.parse(parser, rawArgs, AppArgs.empty) match {
      case Some(appArgs) => Success(appArgs)
      case None => Failure(ArgumentParseException)
    }

  private[this] val parser: OParser[Unit, AppArgs] = {
    val builder = OParser.builder[AppArgs]
    import builder._

    OParser.sequence(
      programName("leioparse"),
      head("LeioParse", "2.0.0", "(c)2018 Reinecke"),
      help('h', "help").text("prints this usage text"),

      opt[String]('f',"format")
      .action((s, args) => args.copy(formatter = formatMapping(s)))
      .validate(s => if (formatMapping.isDefinedAt(s)) success
                   else failure(s"Unrecognized format $s, must be one of ${formatMapping.keys.mkString(", ")}")),

    opt[File]('b', "book-library")
    .action((f, args) => args.copy(bookOverlayPath = f.toPath))
    .validate(f => if (f.exists && f.isFile) success
                   else failure(s"Book library not found at $f")),

    opt[File]('o', "output")
    .action((f, args) => args.copy(outfilePath = Some(f.toPath))),

    arg[File]("input_directory")
    .required()
    .action((dir, args) => args.copy(inputDirPath = dir.toPath))
    .validate(dir => if (dir.exists && dir.isDirectory) success
                     else failure(s"Input directory not found"))
    )
  }

  def formatMapping: Map[String, AppArgs.FormatterArg] = Map(
    "debug" -> FormatterArg.Debug,
    "json" -> FormatterArg.JSON,
    "csv" -> FormatterArg.CSV,
    "legacy" -> FormatterArg.LegacyCSV
  )

  case object ArgumentParseException extends RuntimeException(s"Could not parse application arguments")


}