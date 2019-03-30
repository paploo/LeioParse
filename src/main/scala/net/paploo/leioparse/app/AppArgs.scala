package net.paploo.leioparse.app

import java.nio.file.{Path, Paths}

import net.paploo.leioparse.app.AppArgs.FormatterArg

case class AppArgs(inputDirPath: Path,
                   bookOverlayPath: Path = Paths.get("books.json"),
                   formatter: FormatterArg = FormatterArg.LegacyCSV,
                   outfilePath: Option[Path] = None)

object AppArgs {

  def empty: AppArgs = AppArgs(inputDirPath = Paths.get("."))

  trait FormatterArg
  object FormatterArg {
    case object Debug extends FormatterArg
    case object JSON extends FormatterArg
    case object LegacyCSV extends FormatterArg
    case object CSV extends FormatterArg

    def values: Set[FormatterArg] = Set(Debug, JSON, LegacyCSV, CSV)
  }

}
