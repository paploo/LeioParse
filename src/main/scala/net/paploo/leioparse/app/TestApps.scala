package net.paploo.leioparse.app

import java.nio.file.Paths

import net.paploo.leioparse.app.App.Result
import net.paploo.leioparse.bookoverlayparser.BookOverlayParser
import net.paploo.leioparse.data.leiofile.{LeioBook, LeioSession}
import net.paploo.leioparse.data.overlay.BookOverlay
import net.paploo.leioparse.leiologparser.LeioLogParser
import net.paploo.leioparse.util.extensions.LoggingExtensions.Implicits._
import net.paploo.leioparse.util.extensions.LoggingExtensions.Logging

import scala.concurrent.{ExecutionContext, Future}

object TestApps {

  /*
   * IO NOTES:
   *
   * Byte Oriented Output: OutputStream
   * System.out returns a PrintStream extends OutputStream (print stream adds the println in the old data models)
   * FileOutputStream extends OutputStream, and takes a File.
   *
   * Character (Unicode) Oriented Output: Writer
   * FileWriter extends Writer
   *
   * PrintWriter extends Writer (introduces Println in the new data models)
   *   it can take any Writer or OutputStream
   *
   * CSVWriter wants an Writer or OutputStream
   *
   *
   * def out: OutputStream = ???
   * def writer: Writer = ???
   *
   * def outWriter: Writer = new OutputStreamWriter(out, "UTF-8")
   *
   * def outPrintWriter: PrintWriter = new PrintWriter(out) // under the hood this is new PrintWriter(new BufferedWriter(new OutputStreamWriter(out))), which uses the default charset!
   * def writerPrintWriter: PrintWriter = new PrintWriter(writer)
   *
   * def writerOutputStream: OutputStream = ??? //Cannot be done; Writers can wrap an OutputStream to add the convenience layer, and then you can get the writer back.
   *
   * Also a note on writers: They are side-effecting, which is very NOT functional, requiring that the OutputStream (often wrapped in a PrintWriter) be passed in and modified with side-effects.
   */
  {
    import java.io._
    val out: OutputStream = new ByteArrayOutputStream()
    val pwriter: PrintWriter = new PrintWriter(out)
    pwriter.println("foo")
    pwriter.print("über")
    println(out.toString)
    pwriter.flush()
    println(out.toString)
  }



  object ParseOverlayAndLogApp extends App[Seq[BookOverlay]] with Logging {

    override def run(args: AppArgs)(implicit ec: ExecutionContext):  Future[Result[Seq[BookOverlay]]] = {

      val fp = Paths.get(".", "books.json").normalize()
      BookOverlayParser(fp).parse.log(identity).map(Result.apply)
    }

  }

  object ParseLeioFilesAndLogApp extends App[(Seq[LeioBook], Seq[LeioSession])] with Logging {

    override def run(args: AppArgs)(implicit ec: ExecutionContext): Future[Result[(Seq[LeioBook], Seq[LeioSession])]] = {
      args.log(identity)
      val parser = LeioLogParser.fromPath(args.dataDirPath)
      for {
        books <- parser.parseBooks
        sessions <- parser.parseSessions
        _ = books.log(_.mkString("\n"))
        _ = sessions.log(_.mkString("\n"))
      } yield Result.apply( (books, sessions) )
    }.log(r => s"result = $r")

  }

}
