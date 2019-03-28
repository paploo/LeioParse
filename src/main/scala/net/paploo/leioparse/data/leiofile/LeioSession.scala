package net.paploo.leioparse.data.leiofile

import net.paploo.leioparse.data.core.Book
import net.paploo.leioparse.util.quantities.{DateTime, Location, TimeSpan}

case class LeioSession(bookTitle: Book.Title,
                       startedOn: DateTime,
                       finishedOn: DateTime,
                       duration: TimeSpan,
                       firstPage: Location,
                       lastPage: Location)
