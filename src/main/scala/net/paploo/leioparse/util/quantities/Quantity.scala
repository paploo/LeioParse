package net.paploo.leioparse.util.quantities

import java.time.{Duration, Instant, LocalDateTime, ZoneId}

trait Quantity[N] {
  def value: N
  def toInt: Int
  def toDouble: Double
}

/**
  * Fundamental unit for the measure of elapsed time
  */
case class TimeSpan(value: Duration) extends Quantity[Duration] {
  def +(duration: TimeSpan): TimeSpan = TimeSpan(value plus duration.value)
  def *(ratio: Ratio): TimeSpan = TimeSpan(Duration.ofSeconds((value.getSeconds * ratio.value).round))

  def getMinutes: Double = value.getSeconds / 60.0
  def getHours: Double = value.getSeconds / 3600.0
  def getDays: Double = value.getSeconds / 86400.0

  override def toInt: Int = value.getSeconds.toInt
  override def toDouble: Double = value.getSeconds.toDouble + (value.getNano.toDouble / 1e9)
}

object TimeSpan {
  val Zero: TimeSpan = TimeSpan(Duration.ZERO)
  implicit val Ordering: Ordering[TimeSpan] = scala.math.Ordering.by(span => (span.value.getSeconds, span.value.getNano))
}

/**
  * Fundamental unit for the measure of a date/time, always in the local timezone.
  */
case class DateTime(value: LocalDateTime) extends Quantity[LocalDateTime] {
  def +(duration: TimeSpan): DateTime = DateTime(value plus duration.value)
  def -(dateTime: DateTime): TimeSpan = TimeSpan(Duration.between(value, dateTime.value))

  def toInstant: Instant = value.atZone(ZoneId.systemDefault).toInstant
  def toLong: Long = toInstant.getEpochSecond
  override def toInt: Int = toLong.toInt
  override def toDouble: Double = toInstant.toEpochMilli
}

object DateTime {
  implicit val Ordering: Ordering[DateTime] = scala.math.Ordering.by(dt => (dt.toInstant.getEpochSecond, dt.toInstant.getNano))
}

/**
  * Fundamental unit for the measure of a location, which could be a page, loc, word-offset, etc.
  */
case class Location(value: Int) extends Quantity[Int] {
  def to(endLocInclusive: Location): Blocks = Blocks(endLocInclusive.value - value + 1)
  def until(endLocExclusive: Location): Blocks = Blocks(endLocExclusive.value - value)

  def /(that: Location): Ratio = Ratio.from(this, that)

  override def toInt: Int = value.toInt
  override def toDouble: Double = value.toDouble
}

object Location {
  implicit val Ordering: Ordering[Location] = scala.math.Ordering.by(_.value)
}

/**
  * Fundamental unit of measure of a span of locations; e.g. pages, locs, etc.
  */
case class Blocks(value: Int) extends Quantity[Int] {
  def *(wordDensity: WordDensity): Words = Words((toDouble * wordDensity.value).round.toInt)
  def /(duration: TimeSpan): BlockRate = BlockRate.from(this, duration)

  def /(that: Blocks): Ratio = Ratio.from(this, that)

  override def toInt: Int = value.toInt
  override def toDouble: Double = value.toDouble
}

/**
  * Fundamental unit.
  */
case class Words(value: Int) extends Quantity[Int] {
  def /(blocks: Blocks): WordDensity = WordDensity.from(this, blocks)
  def /(duration: TimeSpan): WordRate = WordRate.from(this, duration)

  def /(that: Words): Ratio = Ratio.from(this, that)

  override def toInt: Int = value.toInt
  override def toDouble: Double = value.toDouble
}

/**
  * Derived value: Words / Blocks
  */
case class WordDensity(value: Double) extends Quantity[Double] {
  def *(blocks: Blocks): Words = blocks * this

  override def toInt: Int = value.toInt
  override def toDouble: Double = value.toDouble
}

object WordDensity {
  def from(words: Words, blocks: Blocks): WordDensity = WordDensity(words.toDouble / blocks.toDouble)
}

/**
  * Derived value: Blocks / Hour
  */
case class BlockRate(value: Double) extends Quantity[Double] {
  def inverse: BlockPace = BlockPace(60.0 / value)

  override def toInt: Int = value.toInt
  override def toDouble: Double = value.toDouble
}

object BlockRate {
  def from(blocks: Blocks, duration: TimeSpan): BlockRate = BlockRate(blocks.toDouble / duration.getHours)
}

/**
  * Derive value: Minutes / Block
  */
case class BlockPace(value: Double) extends Quantity[Double] {
  def inverse: BlockRate = BlockRate(60.0 / value)

  override def toInt: Int = value.toInt
  override def toDouble: Double = value.toDouble
}

object BlockPace {
  def from(duration: TimeSpan, blocks: Blocks): BlockPace = BlockPace(duration.getMinutes / blocks.toDouble)
}

/**
  * Derived value: Words / Minute
  */
case class WordRate(value: Double) extends Quantity[Double] {
  override def toInt: Int = value.toInt
  override def toDouble: Double = value.toDouble
}

object WordRate {
  def from(words: Words, duration: TimeSpan): WordRate = WordRate(words.toDouble / duration.getMinutes)
}

/**
  * Derived value: Blocks/Day, used for tracking a whole book across the wall-clock, rather than just during sessions.
  */
case class BlockDailyRate(value: Double) extends Quantity[Double] {
  override def toInt: Int = value.toInt
  override def toDouble: Double = value.toDouble
}

object BlockDailyRate {
  def from(blocks: Blocks, duration: TimeSpan): BlockDailyRate = BlockDailyRate(blocks.toDouble / duration.getDays)
}

case class Ratio(value: Double) extends Quantity[Double] {
  def inverse: Ratio = Ratio(1.0 / value)

  override def toInt: Int = value.toInt
  override def toDouble: Double = value.toDouble
}

/**
  * Derived value: Stores the ratio between two quantities
  */
object Ratio {
  def from[A <: Quantity[_]](numerator: A, denominator: A): Ratio = Ratio(numerator.toDouble / denominator.toDouble)
}
