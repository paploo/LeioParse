package net.paploo.leioparse.data.core

import cats.Show
import net.paploo.leioparse.util.quantities._

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
}

object Session {

  implicit val ShowSession: Show[Session] =
    s => (s.productIterator ++ List(s.blocks, s.endDate, s.blockRate, s.blockPace)).mkString(s"${s.productPrefix}(", ", ", ")")

}
