package net.paploo.leioparse.v1.data

import java.time.{Duration, LocalDateTime}

case class Session(book: Book,
                   date: LocalDateTime,
                   duration: Duration,
                   pages: Int) {
  def paceInSecondsPerPage: Double = duration.getSeconds.toDouble / pages.toDouble
  def rateInPagesPerHour: Double = 3600.0 / paceInSecondsPerPage

  def rateInWordsPerMinute: Option[Double] = book.wordsPerPage.map(_.value * rateInPagesPerHour / 60.0)
}
