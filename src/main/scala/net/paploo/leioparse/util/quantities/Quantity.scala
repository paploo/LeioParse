package net.paploo.leioparse.util.quantities

import java.time.{Duration, Instant, LocalDateTime, ZoneId}

//TODO: Formalize a number of the math operations via Numeric type classes.

trait Quantity[N] {
  def value: N

  def toInt: Int
  def toDouble: Double

  def isZero: Boolean
}

/**
  * Fundamental unit for the measure of elapsed time
  */
case class TimeSpan(value: Duration) extends Quantity[Duration] {
  def +(duration: TimeSpan): TimeSpan = TimeSpan(value plus duration.value)
  def *(ratio: Ratio): TimeSpan = TimeSpan(Duration.ofSeconds((value.getSeconds * ratio.value).round))

  def toSeconds: Long = value.getSeconds
  def toMinutes: Double = value.getSeconds / 60.0
  def toHours: Double = value.getSeconds / 3600.0
  def toDays: Double = value.getSeconds / 86400.0

  def toDuration: Duration = value
  override def toInt: Int = value.getSeconds.toInt
  override def toDouble: Double = value.getSeconds.toDouble + (value.getNano.toDouble / 1e9)

  override def isZero: Boolean = value.getSeconds == 0 //Discard nanos
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
  def -(duration: TimeSpan): DateTime = DateTime(value minus duration.value)
  def -(dateTime: DateTime): TimeSpan = TimeSpan(Duration.between(dateTime.value, value))

  private def toInstant: Instant = value.atZone(ZoneId.systemDefault).toInstant
  def toLocalDateTime: LocalDateTime = value
  def toLong: Long = toInstant.getEpochSecond
  override def toInt: Int = toLong.toInt
  override def toDouble: Double = (toInstant.toEpochMilli / 1000.0) + (toInstant.getNano / 1e9)

  override def isZero: Boolean = toInstant.getEpochSecond == 0L
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

  override def isZero: Boolean = value == 0
}

object Location {
  val Zero: Location = Location(0)
  implicit val Ordering: Ordering[Location] = scala.math.Ordering.by(_.value)
}

/**
  * Fundamental unit of measure of a span of locations; e.g. pages, locs, etc.
  */
case class Blocks(value: Int) extends Quantity[Int] {
  def *(ratio: Ratio): Blocks = Blocks((value * ratio.value).round.toInt)
  def *(wordDensity: WordDensity): Words = Words((toDouble * wordDensity.value).round.toInt)

  def /(duration: TimeSpan): BlockRate = BlockRate.from(this, duration)
  def /(that: Blocks): Ratio = Ratio.from(this, that)

  def +(that: Blocks): Blocks = Blocks(value + that.value)
  def -(that: Blocks): Blocks = Blocks(value - that.value)

  override def toInt: Int = value.toInt
  override def toDouble: Double = value.toDouble

  override def isZero: Boolean = value == 0
}

object Blocks {
  val Zero: Blocks = Blocks(0)
  implicit val Ordering: Ordering[Blocks] = scala.math.Ordering.by(_.value)
}

/**
  * Fundamental unit of counts of words.
  */
case class Words(value: Int) extends Quantity[Int] {
  def +(that: Words): Words = Words(value + that.value)

  def /(blocks: Blocks): WordDensity = WordDensity.from(this, blocks)
  def /(duration: TimeSpan): WordRate = WordRate.from(this, duration)

  def /(that: Words): Ratio = Ratio.from(this, that)

  def *(ratio: Ratio): Words = Words((value * ratio.value).round.toInt)

  override def toInt: Int = value.toInt
  override def toDouble: Double = value.toDouble

  override def isZero: Boolean = value == 0
}

object Words {
  val Zero: Words = Words(0)
  implicit val Ordering: Ordering[Words] = scala.math.Ordering.by(_.value)
}

/**
  * Derived value: Words / Blocks
  */
case class WordDensity(value: Double) extends Quantity[Double] {
  def *(blocks: Blocks): Words = blocks * this

  override def toInt: Int = value.toInt
  override def toDouble: Double = value.toDouble

  override def isZero: Boolean = value == 0.0
}

object WordDensity {
  val Zero: WordDensity = WordDensity(0.0)
  implicit val Ordering: Ordering[WordDensity] = scala.math.Ordering.by(_.value)

  def from(words: Words, blocks: Blocks): WordDensity = WordDensity(words.toDouble / blocks.toDouble)
}

/**
  * Derived value: Blocks / Hour
  */
case class BlockRate(value: Double) extends Quantity[Double] {
  def inverse: BlockPace = BlockPace(60.0 / value)

  override def toInt: Int = value.toInt
  override def toDouble: Double = value.toDouble

  override def isZero: Boolean = value == 0.0
}

object BlockRate {
  val Zero: BlockRate = BlockRate(0.0)
  implicit val Ordering: Ordering[BlockRate] = scala.math.Ordering.by(_.value)

  def from(blocks: Blocks, duration: TimeSpan): BlockRate = BlockRate(blocks.toDouble / duration.toHours)
}

/**
  * Derive value: Minutes / Block
  */
case class BlockPace(value: Double) extends Quantity[Double] {
  def inverse: BlockRate = BlockRate(60.0 / value)

  override def toInt: Int = value.toInt
  override def toDouble: Double = value.toDouble

  override def isZero: Boolean = value == 0.0
}

object BlockPace {
  val Zero: BlockPace = BlockPace(0.0)
  implicit val Ordering: Ordering[BlockPace] = scala.math.Ordering.by(_.value)

  def from(duration: TimeSpan, blocks: Blocks): BlockPace = BlockPace(duration.toMinutes / blocks.toDouble)
}

/**
  * Derived value: Words / Minute
  */
case class WordRate(value: Double) extends Quantity[Double] {
  override def toInt: Int = value.toInt
  override def toDouble: Double = value.toDouble

  override def isZero: Boolean = value == 0.0
}

object WordRate {
  val Zero: WordRate = WordRate(0.0)
  implicit val Ordering: Ordering[WordRate] = scala.math.Ordering.by(_.value)

  def from(words: Words, duration: TimeSpan): WordRate = WordRate(words.toDouble / duration.toMinutes)
}

/**
  * Derived value: Blocks/Day, used for tracking a whole book across the wall-clock, rather than just during sessions.
  */
case class BlockDailyRate(value: Double) extends Quantity[Double] {
  override def toInt: Int = value.toInt
  override def toDouble: Double = value.toDouble

  override def isZero: Boolean = value == 0.0
}

object BlockDailyRate {
  val Zero: BlockDailyRate = BlockDailyRate(0.0)
  implicit val Ordering: Ordering[BlockDailyRate] = scala.math.Ordering.by(_.value)

  def from(blocks: Blocks, duration: TimeSpan): BlockDailyRate = BlockDailyRate(blocks.toDouble / duration.toDays)
}

/**
  * Derived value: Stores the ratio between two quantities
  */
case class Ratio(value: Double) extends Quantity[Double] {
  def inverse: Ratio = Ratio(1.0 / value)

  override def toInt: Int = value.toInt
  override def toDouble: Double = value.toDouble

  override def isZero: Boolean = value == 0.0
}

object Ratio {
  def Zero: Ratio = Ratio(0.0)
  implicit val Ordering: Ordering[Ratio] = scala.math.Ordering.by(_.value)

  def from[A <: Quantity[_]](numerator: A, denominator: A): Ratio = Ratio(numerator.toDouble / denominator.toDouble)
}
