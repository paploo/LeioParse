package net.paploo.leioparse.leiologparser.data

import net.paploo.leioparse.data.core.Book
import net.paploo.leioparse.util.quantities.{DateTime, Location}


case class LeioSession(bookTitle: Book.Title,
                       startedOn: DateTime,
                       finishedOn: DateTime,
                       firstPage: Location,
                       lastPage: Location)
