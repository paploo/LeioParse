package net.paploo.leioparse.compositor

import net.paploo.leioparse.data.core.{Book, BookSessions, Session}
import net.paploo.leioparse.util.library.Library
import net.paploo.leioparse.util.library.Library.OptionLibrary

trait BookSessionsCompositor extends (Seq[Session] => Seq[Book] => Seq[BookSessions])

object BookSessionsCompositor {

  def apply: BookSessionsCompositor = new StandardBookSessionsCompositor

  private class StandardBookSessionsCompositor extends BookSessionsCompositor {

    override def apply(sessions: Seq[Session]): Seq[Book] => Seq[BookSessions] = books => {
      ???
    }

    private[this] def bookLibrary(books: Seq[Book]): OptionLibrary[Book.Title, Book] =
      Library.fromSeq(books)(_.title)

  }

}
