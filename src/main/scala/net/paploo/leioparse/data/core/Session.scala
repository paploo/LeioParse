package net.paploo.leioparse.data.core

import cats.Show
import net.paploo.leioparse.util.quantities._

/**
  * Represents a session spent reading a given book.
  * @param bookTitle The title of the book—this is usually used as the primary reference key to a book
  * @param startDate The date-time that the session began.
  * @param duration The duration of the reading session.
  * @param startLocation The location that reading started from (inclusive).
  * @param endLocation The location from which reading completed (inclusive).
  */
case class Session(bookTitle: Book.Title,
                   startDate: DateTime,
                   duration: TimeSpan,
                   startLocation: Location,
                   endLocation: Location) {
  val blocks: Blocks = startLocation to endLocation
  val endDate: DateTime = startDate + duration
  val blockRate: BlockRate = blocks / duration
  val blockPace: BlockPace = blockRate.inverse

  def words(averageWordDensity: WordDensity): Words = blocks * averageWordDensity
  def wordRate(averageWordDensity: WordDensity): WordRate = words(averageWordDensity) / duration

  def sessionRelativeStart(tZero: DateTime): TimeSpan = startDate - tZero
  def sessionRelativeEnd(tZero: DateTime): TimeSpan = endDate - tZero
}

object Session {

  implicit val ShowSession: Show[Session] =
    s => (s.productIterator ++ List(s.blocks, s.endDate, s.blockRate, s.blockPace)).mkString(s"${s.productPrefix}(", ", ", ")")

}
