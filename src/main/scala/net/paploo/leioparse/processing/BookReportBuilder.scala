package net.paploo.leioparse.processing

import net.paploo.leioparse.data.core.{Book, BookReport, Session}
import net.paploo.leioparse.util.library.Library
import net.paploo.leioparse.util.library.Library.OptionLibrary
import net.paploo.leioparse.util.extensions.Implicits._
import net.paploo.leioparse.util.extensions.LoggingExtensions.Logging

trait BookReportBuilder extends (Seq[Session] => Seq[BookReport])


object BookReportBuilder {

  def withBookLibrary(bookLibrary: OptionLibrary[Book.Title, Book]): BookReportBuilder = new StandardBookReportCompositor(bookLibrary)

  def withBooks(books: Seq[Book]): BookReportBuilder = withBookLibrary(Library.fromSeq(books)(_.title))

  private class StandardBookReportCompositor(bookLibrary: OptionLibrary[Book.Title, Book]) extends BookReportBuilder with Logging {

    override def apply(sessions: Seq[Session]): Seq[BookReport] = sessions.orderedGroupBy(_.bookTitle).flatMap {
      case (title, sessions) =>
        bookLibrary(title) match {
          case Some(book) =>
            Seq(BookReport.from(book, sessions))
          case None =>
            //I can either return an empty seq to filter out bookless sessions (inner join), or make a fake book and keep the session rows but with bad data.
            //Since this will only happen if someone removes the book from the file generated by leio, I choose to log and skip the book for now.
            logger.warn(s"Could not find book in library for $title; ${sessions.length} sessions skipped!")
            Seq.empty
        }
    }

  }

}