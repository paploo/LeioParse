package net.paploo.leioparse.data.overlay

import net.paploo.leioparse.data.core.Book
import net.paploo.leioparse.util.quantities.WordDensity

case class BookOverlay(title: Book.Title, //Required because this is how we link them!
                       identifier: Option[Book.Id],
                       wordDensity: Option[WordDensity])
