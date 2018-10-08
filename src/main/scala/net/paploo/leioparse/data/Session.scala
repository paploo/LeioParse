package net.paploo.leioparse.data

import java.time.{Duration, LocalDateTime}

case class Session(book: Book,
                   date: LocalDateTime,
                   duration: Duration,
                   pages: Int) {
  def paceInSecondsPerPage: Double = duration.getSeconds.toDouble / pages.toDouble
  def rateInPagesPerHour: Double = 3600.0 / paceInSecondsPerPage

  def rateInWordsPerMinute: Option[Double] = book.data.worsePerPage.map(_ * rateInPagesPerHour / 60.0)
}
