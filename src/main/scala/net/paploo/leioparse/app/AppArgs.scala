package net.paploo.leioparse.app

import java.nio.file.{Path, Paths}

import net.paploo.leioparse.app.AppArgs.{FormatterArg, OutputMethod}

import scala.concurrent.Promise

case class AppArgs(inputDirPath: Path,
                   bookOverlayPath: Path = Paths.get("books.json"),
                   formatter: FormatterArg = FormatterArg.CSV,
                   outputMethod: OutputMethod = OutputMethod.StdOut)

object AppArgs {

  def empty: AppArgs = AppArgs(inputDirPath = Paths.get("."))

  trait OutputMethod
  object OutputMethod {
    /** //Output to System.out (stdout) */
    case object StdOut extends OutputMethod
    /** Output the lines as UTF-8 strings to the given promise. This is usually used for test scenarios. */
    case class Lines(linesPromise: Promise[Seq[String]]) extends OutputMethod
    /** Output to the given file **/
    case class FilePath(toPath: Path) extends OutputMethod
  }

  trait FormatterArg
  object FormatterArg {
    case object Debug extends FormatterArg
    case object JSON extends FormatterArg
    case object LegacyCSV extends FormatterArg
    case object CSV extends FormatterArg

    def values: Set[FormatterArg] = Set(Debug, JSON, LegacyCSV, CSV)
  }

}
