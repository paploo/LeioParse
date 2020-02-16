package net.paploo.leioparse.processing

import net.paploo.leioparse.data.core.Book
import net.paploo.leioparse.data.leiofile.LeioBook
import net.paploo.leioparse.data.overlay.BookOverlay
import net.paploo.leioparse.util.extensions.Implicits._
import net.paploo.leioparse.util.extensions.LoggingExtensions.Logging
import net.paploo.leioparse.util.library.Library
import net.paploo.leioparse.util.library.Library.OptionLibrary
import net.paploo.leioparse.util.quantities.{Blocks, WordDensity}

/**
  * Produces a converter of LeioBook to Book, incorporating an appropriate overlay from the given overlays.
  */
trait BookBuilder extends (LeioBook => Book)

object BookBuilder {

  def withOverlayLibrary(overlayLibrary: OptionLibrary[Book.Title, BookOverlay]): BookBuilder = new StandardBookCompositor(overlayLibrary)

  def withOverlays(overlays: Seq[BookOverlay]): BookBuilder = withOverlayLibrary(Library.fromSeq(overlays)(_.title))

  private class StandardBookCompositor(overlayLibrary: OptionLibrary[Book.Title, BookOverlay]) extends BookBuilder with Logging {

    override def apply(leioBook: LeioBook): Book =
      makeBook(leioBook, overlayLibrary(leioBook.title))

    private[this] def makeBook(leioBook: LeioBook, overlay: Option[BookOverlay]): Book = Book(
      title = leioBook.title,
      startLocation = leioBook.firstPage,
      endLocation = leioBook.lastPage,
      averageWordDensity = overlay.flatMap(_.wordDensity) getOrElse defaultWordDensity(leioBook),
      externalId = overlay.flatMap(_.externalId),
    )

    private[this] def defaultWordDensity(book: LeioBook): WordDensity = ((book.lastPage to book.firstPage) match {
      case Blocks(n) if n <= 4000 => WordDensity(300) //Probably measured in pages
      case Blocks(_) => WordDensity(21) //Probably measured in locs
    }) tap { wd => logger.warn(s"No word density provided in overlay for ${book.title}; defaulting to $wd") }

  }

}
