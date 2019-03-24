package net.paploo.leioparse.compositor

import net.paploo.leioparse.data.core.Book
import net.paploo.leioparse.data.leiofile.LeioBook
import net.paploo.leioparse.data.overlay.BookOverlay
import net.paploo.leioparse.util.library.Library
import net.paploo.leioparse.util.library.Library.OptionLibrary
import net.paploo.leioparse.util.quantities.{Blocks, WordDensity}

/**
  * Produces a converter of LeioBook to Book, incorporating an appropriate overlay from the given overlays.
  */
trait BookCompositor extends (Seq[BookOverlay] => LeioBook => Book)

object BookCompositor {

  def apply: BookCompositor = new StandardBookCompositor

  private class StandardBookCompositor extends BookCompositor {

    override def apply(overlays: Seq[BookOverlay]): LeioBook => Book =
      leioBook => makeBook(leioBook, bookOverlayLibrary(overlays)(leioBook.title))

    private[this] def bookOverlayLibrary(overlays: Seq[BookOverlay]): OptionLibrary[Book.Title, BookOverlay] =
      Library.fromSeq(overlays)(_.title)

    private[this] def makeBook(leioBook: LeioBook, overlay: Option[BookOverlay]): Book = Book(
      identifier = overlay.flatMap(_.identifier),
      title = leioBook.title,
      startLocation = leioBook.firstPage,
      endLocation = leioBook.lastPage,
      averageWordDensity = overlay.flatMap(_.wordDensity) getOrElse defaultWordDensity(leioBook)
    )

    private[this] def defaultWordDensity(book: LeioBook): WordDensity = (book.lastPage to book.firstPage) match {
      case Blocks(n) if n <= 10000 => WordDensity(300) //Probably measured in pages
      case Blocks(_) => WordDensity(22) //Probably measured in locs
    }

  }

}
