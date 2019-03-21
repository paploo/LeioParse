package net.paploo.leioparse.logparser.data

import net.paploo.leioparse.util.quantities.{DateTime, Location}

case class LeioSession(bookTitle: String,
                       startedOn: Option[DateTime],
                       finishedOn: Option[DateTime],
                       firstPage: Location,
                       lastPage: Location)
