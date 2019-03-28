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
