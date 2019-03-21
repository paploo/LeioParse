package net.paploo.leioparse.logparser.pipeline.parser

import java.time.{Duration, LocalDateTime}
import java.time.format.DateTimeFormatter

import net.paploo.leioparse.data.core.Book
import net.paploo.leioparse.logparser.pipeline.LeioParsePipeline.Row
import net.paploo.leioparse.util.quantities.{DateTime, Location, TimeSpan}

import scala.util.matching.Regex

//Don't implement function or the compiler will try to use these as implicit conversions!
trait ValueParser[A] {
  def apply(v: Row.Value): A

  def toFunction: Row.Value => A = apply
  def map[B](f: A => B): ValueParser[B] = s => f(apply(s))
}

object ValueParser {

  def apply[A](implicit valueParser: ValueParser[A]) = valueParser

  def parse[A](value: Row.Value)(implicit parse: ValueParser[A]): A = parse(value)

  implicit val StringValueParser: ValueParser[String] = v => v.value.toString

  implicit val IntValueParser: ValueParser[Int] = v => v.value.toInt

  implicit val BookTitleValueParser: ValueParser[Book.Title] = StringValueParser map Book.Title.apply

  implicit val LocationValueParser: ValueParser[Location] = IntValueParser map Location.apply

  implicit val DateTimeParser: ValueParser[DateTime] =
    v => DateTime(LocalDateTime.parse(v.value, DateTimeFormatter.ofPattern("M/d/yy HH:mm")))

  implicit val TimeSpanParser: ValueParser[TimeSpan] = new ValueParser[TimeSpan] {

    private[this] val leioPattern: Regex ="""((\d+) h)?\s*((\d+) min)\s*?((\d+) s)?""".r

    override def apply(v: Row.Value): TimeSpan = TimeSpan(v.value match {
                                                            case leioPattern(_, h, _, m, _, s) =>
                                                              Duration.ofHours(
                                                                Option(h).map(_.toLong).getOrElse(0L)
                                                              ).plusMinutes(
                                                                Option(m).map(_.toLong).getOrElse(0L)
                                                              ).plusSeconds(
                                                                Option(s).map(_.toLong).getOrElse(0L)
                                                              )
                                                          })
  }

}
