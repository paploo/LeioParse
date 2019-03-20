package net.paploo.leioparse.data.core

import net.paploo.leioparse.util.quantities._

case class Session(book: Book,
                   startDate: DateTime,
                   duration: TimeSpan,
                   startLocation: Location,
                   endLocation: Location) {
  val blocks: Blocks = startLocation to endLocation
  val endDate: DateTime = startDate + duration
  val words: Words = blocks * book.averageWordsPerBlock
  val blockRate: BlockRate = blocks / duration
  val blockPace: BlockPace = blockRate.inverse
  val wordRate: WordRate = words / duration
}
