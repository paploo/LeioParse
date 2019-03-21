package net.paploo.leioparse.logparser.data

import net.paploo.leioparse.data.core.Book
import net.paploo.leioparse.util.quantities.Location

case class LeioBook(title: Book.Title,
                    firstPage: Location,
                    lastPage: Location)