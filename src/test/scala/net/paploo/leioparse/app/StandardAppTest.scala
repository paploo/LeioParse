package net.paploo.leioparse.app

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}

import net.paploo.leioparse.app.AppArgs.OutputMethod
import net.paploo.leioparse.test.{LineDiffSupport, TestSpec}

import scala.concurrent.{ExecutionContext, Promise}

class StandardAppTest extends TestSpec with LineDiffSupport {

  implicit val ec: ExecutionContext = ExecutionContext.global

  describe(s"Legacy Output") {

    def appArgs(linesPromise: Promise[Seq[String]]): AppArgs = {
      val inputDirPath: Path = Paths.get(getClass.getClassLoader.getResource("leio_sample_data").toURI)
      val bookOverlayPath: Path = Paths.get(getClass.getClassLoader.getResource("sample_legacy_books.json").toURI)
      AppArgs(inputDirPath = inputDirPath,
              bookOverlayPath = bookOverlayPath,
              formatter = AppArgs.FormatterArg.LegacyCSV,
              outputMethod = OutputMethod.Lines(linesPromise))
    }

    lazy val legacyOutputLines: Seq[String] = {
      //Use expected_legacy_out.csv, which has minor adjustments due to rounding errors compared with the *true* original, sample_legacy_out.csv
      val legacyOutFile: Path = Paths.get(getClass.getClassLoader.getResource("expected_legacy_out.csv").toURI)
      StandardAppTest.readFile(legacyOutFile)
    }

    val app = StandardApp

    it("should be able to return legacy csv that matches output from v1") {
      val linesPromise = Promise[Seq[String]]()
      val args = appArgs(linesPromise)

      app.run(args)
      val outputLines = linesPromise.future.futureValue

      assertNoLineDifference(outputLines, legacyOutputLines)
    }

  }

  describe(s"JSON Output") {

    def appArgs(linesPromise: Promise[Seq[String]]): AppArgs = {
      val inputDirPath: Path = Paths.get(getClass.getClassLoader.getResource("leio_sample_data").toURI)
      val bookOverlayPath: Path = Paths.get(getClass.getClassLoader.getResource("sample_legacy_books.json").toURI)
      AppArgs(inputDirPath = inputDirPath,
              bookOverlayPath = bookOverlayPath,
              formatter = AppArgs.FormatterArg.JSON,
              //outputMethod = OutputMethod.StdOut)
              outputMethod = OutputMethod.Lines(linesPromise))
    }

    lazy val legacyOutputLines: Seq[String] = {
      val legacyOutFile: Path = Paths.get(getClass.getClassLoader.getResource("expected_json_out.json").toURI)
      StandardAppTest.readFile(legacyOutFile)
    }

    val app = StandardApp

    it("should be able to return legacy csv that matches output from v1") {
      val linesPromise = Promise[Seq[String]]()
      val args = appArgs(linesPromise)

      app.run(args)
      val outputLines = linesPromise.future.futureValue

      assertNoLineDifference(outputLines, legacyOutputLines)
    }

  }

  describe(s"V2CSV Output") {

    def appArgs(linesPromise: Promise[Seq[String]]): AppArgs = {
      val inputDirPath: Path = Paths.get(getClass.getClassLoader.getResource("leio_sample_data").toURI)
      val bookOverlayPath: Path = Paths.get(getClass.getClassLoader.getResource("sample_legacy_books.json").toURI)
      AppArgs(inputDirPath = inputDirPath,
              bookOverlayPath = bookOverlayPath,
              formatter = AppArgs.FormatterArg.CSV,
              //outputMethod = OutputMethod.StdOut)
              outputMethod = OutputMethod.Lines(linesPromise))
    }

    lazy val legacyOutputLines: Seq[String] = {
      val legacyOutFile: Path = Paths.get(getClass.getClassLoader.getResource("expected_csv_out.csv").toURI)
      StandardAppTest.readFile(legacyOutFile)
    }

    val app = StandardApp

    it("should be able to return legacy csv that matches output from v1") {
      val linesPromise = Promise[Seq[String]]()
      val args = appArgs(linesPromise)

      app.run(args)
      val outputLines = linesPromise.future.futureValue

      assertNoLineDifference(outputLines, legacyOutputLines)
    }

  }

}

object StandardAppTest {

  def readFile(path: Path): Seq[String] = {
    import scala.collection.JavaConverters._
    Files.readAllLines(path, StandardCharsets.UTF_8).asScala
  }

}