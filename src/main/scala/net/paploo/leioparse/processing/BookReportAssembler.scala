package net.paploo.leioparse.processing

import cats.Functor
import net.paploo.leioparse.data.core.BookReport
import net.paploo.leioparse.data.leiofile.{LeioBook, LeioSession}
import net.paploo.leioparse.data.overlay.BookOverlay
import net.paploo.leioparse.util.extensions.Implicits._

trait BookReportAssembler {
  def assemble(leioSessions: Seq[LeioSession], leioBooks: Seq[LeioBook], bookOverlays: Seq[BookOverlay]): Seq[BookReport]
}

object BookReportAssembler {

  val default: BookReportAssembler = new DefaultBookReportAssembler

  private class DefaultBookReportAssembler extends BookReportAssembler {

    override def assemble(leioSessions: Seq[LeioSession], leioBooks: Seq[LeioBook], bookOverlays: Seq[BookOverlay]): Seq[BookReport] = {
      val sessionsBuilder = Functor[Seq].lift(SessionBuilder.default)
      val booksBuilder = Functor[Seq].lift(BookBuilder.withOverlays(bookOverlays))
      BookReportBuilder.withBooks(booksBuilder(leioBooks))(sessionsBuilder(leioSessions))
    }

  }

}
