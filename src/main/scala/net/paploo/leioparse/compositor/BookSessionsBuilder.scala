package net.paploo.leioparse.compositor

import net.paploo.leioparse.data.core.{Book, BookSessions, Session}
import net.paploo.leioparse.util.library.Library
import net.paploo.leioparse.util.library.Library.OptionLibrary
import net.paploo.leioparse.util.extensions.Implicits._
import net.paploo.leioparse.util.extensions.LoggingExtensions.Logging

trait BookSessionsBuilder extends (Seq[Session] => Seq[BookSessions])


object BookSessionsBuilder {

  def withBookLibrary(bookLibrary: OptionLibrary[Book.Title, Book]): BookSessionsBuilder = new StandardBookSessionsCompositor(bookLibrary)

  def withBooks(books: Seq[Book]): BookSessionsBuilder = withBookLibrary(Library.fromSeq(books)(_.title))

  private class StandardBookSessionsCompositor(bookLibrary: OptionLibrary[Book.Title, Book]) extends BookSessionsBuilder with Logging {

    override def apply(sessions: Seq[Session]): Seq[BookSessions] = sessions.orderedGroupBy(_.bookTitle).flatMap {
      case (title, sessions) =>
        bookLibrary(title) match {
          case Some(book) =>
            Seq(BookSessions.from(book, sessions))
          case None =>
            logger.warn(s"Could not find book in library for $title")
            //I can either return an empty seq to filter out bookless sessions (inner join), or make a fake book and keep the session rows but with bad data.
            Seq.empty
        }
    }

  }

}
