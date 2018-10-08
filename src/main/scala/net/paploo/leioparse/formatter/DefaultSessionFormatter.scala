package net.paploo.leioparse.formatter

import java.time.format.DateTimeFormatter

import net.paploo.leioparse.data.Session

class DefaultSessionFormatter extends (Session => Seq[String]) {

  override def apply(session: Session): Seq[String] = Seq(
    session.book.data.title,
    session.book.id.value.toString,
    session.book.data.worsePerPage.map(_.toString).getOrElse(""),
    session.date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    session.pages.toString,
    session.duration.getSeconds.toString,
    session.rateInPagesPerHour.formatted("%.1f"),
    session.paceInSecondsPerPage.formatted("%.1f"),
    session.rateInWordsPerMinute.map(_.formatted("%.1f")).getOrElse("")
  )

}

object DefaultSessionFormatter {

  val headers: Seq[String] = Seq(
    "Book",
    "Book_ID",
    "Book_Words_Per_Page",
    "Date",
    "Pages",
    "Duration_Sec",
    "Rate_PPH",
    "Pace_SPP",
    "Rate_WPM"
  )


}
