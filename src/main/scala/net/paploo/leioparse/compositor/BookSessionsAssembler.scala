package net.paploo.leioparse.compositor

import cats.Functor
import net.paploo.leioparse.data.core.BookSessions
import net.paploo.leioparse.data.leiofile.{LeioBook, LeioSession}
import net.paploo.leioparse.data.overlay.BookOverlay
import net.paploo.leioparse.util.extensions.Implicits._

trait BookSessionsAssembler {
  def assemble(leioSessions: Seq[LeioSession], leioBooks: Seq[LeioBook], bookOverlays: Seq[BookOverlay]): Seq[BookSessions]
}

object BookSessionsAssembler {

  val default: BookSessionsAssembler = new DefaultBookSessionsAssembler

  private class DefaultBookSessionsAssembler extends BookSessionsAssembler {

    override def assemble(leioSessions: Seq[LeioSession], leioBooks: Seq[LeioBook], bookOverlays: Seq[BookOverlay]): Seq[BookSessions] = {
      val sessionsBuilder = Functor[Seq].lift(SessionBuilder.default)
      val booksBuilder = Functor[Seq].lift(BookBuilder.withOverlays(bookOverlays))
      BookSessionsBuilder.withBooks(booksBuilder(leioBooks))(sessionsBuilder(leioSessions))
    }

  }

}
