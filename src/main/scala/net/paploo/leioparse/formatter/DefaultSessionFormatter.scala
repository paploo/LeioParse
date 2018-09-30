package net.paploo.leioparse.formatter

import java.time.format.DateTimeFormatter

import net.paploo.leioparse.Session
import net.paploo.leioparse.parser.Row.Column

class DefaultSessionFormatter extends (Session => Seq[String]) {

  override def apply(session: Session): Seq[String] = Seq(
    session.bookName,
    session.date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    session.pages.toString,
    session.duration.getSeconds.toString,
    session.rateInPagesPerHour.formatted("%.1f"),
    session.paceInSecondsPerPage.formatted("%.1f")
  )

}

object DefaultSessionFormatter {

  val headers: Seq[String] = Seq(
    "Book",
    "Date",
    "Pages",
    "Duration_Sec",
    "Rate_PPH",
    "Pace_SPP"
  )


}
