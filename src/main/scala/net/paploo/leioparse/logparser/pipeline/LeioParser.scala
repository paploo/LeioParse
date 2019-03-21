package net.paploo.leioparse.logparser.pipeline

import java.time.{Duration, LocalDateTime}
import java.time.format.DateTimeFormatter

import net.paploo.leioparse.data.core.Book
import net.paploo.leioparse.logparser.data.{LeioBook, LeioSession}
import net.paploo.leioparse.logparser.pipeline.LeioParsePipeline.Row
import net.paploo.leioparse.util.quantities._

import scala.util.{Failure, Success, Try}
import scala.util.matching.Regex

trait LeioParser[A] extends (Row => Try[A])

object LeioParser {

  trait ParseTools {
    implicit class RichRow(toRow: Row) {
      def extractRequired[A](key: Row.Key)(implicit parser: ValueParser[A]): Try[A] = extract(key) match {
        case Some(extracted) => Success(extracted)
        case None => Failure(new NoSuchElementException(s"Could not extract required key $key from $toRow"))
      }

      def extractOptional[A](key: Row.Key)(implicit parser: ValueParser[A]): Try[Option[A]] = Try(extract(key))

      def extract[A](key: Row.Key)(implicit parser: ValueParser[A]): Option[A] = toRow.get(key).map(parser.toFunction)
    }
  }

  private class SessionParser extends LeioParser[LeioSession] with ParseTools {
    import SessionParser.Keys._

    override def apply(row: Row): Try[LeioSession] = for {
      bookTitle <- row.extractRequired[Book.Title](BookTitle)
      startedOn <- extractStartedOn(row)
      finishedOn <- row.extractRequired[DateTime](FinishedOn)
      firstPage <- row.extractRequired[Location](FirstPage)
      lastPage <- row.extractRequired[Location](LastPage)
    } yield LeioSession(
      bookTitle,
      startedOn,
      finishedOn,
      firstPage,
      lastPage
    )

    //When Leio gets a manually entered record with a time and duration, the first page is missing, so sometimes we have to infer.
    private[this] def extractStartedOn(row: Row): Try[DateTime] = row.extract[DateTime](StartedOn) match {
      case Some(dt) => Success(dt)
      case None => for {
        finishedOn <- row.extractRequired[DateTime](FinishedOn)
        duration <- row.extractRequired[TimeSpan](SessionDuration)
      } yield finishedOn - duration
    }
  }

  private object SessionParser {
    def apply: LeioParser[LeioSession] = new SessionParser

    object Keys {
      val BookTitle = Row.Key("Book")
      val StartedOn = Row.Key("Started On")
      val FinishedOn = Row.Key("Finished On")
      val FirstPage = Row.Key("First Page")
      val LastPage = Row.Key("Last Page")
      val SessionDuration = Row.Key("Duration")
    }
  }

  private class BookParser extends LeioParser[LeioBook] with ParseTools {
    import BookParser.Keys._

    override def apply(row: Row): Try[LeioBook] = for {
      title <- row.extractRequired[Book.Title](Title)
      firstPage <- row.extractRequired[Location](FirstPage)
      lastPage <- row.extractRequired[Location](LastPage)
    } yield LeioBook(
      title = title,
      firstPage = firstPage,
      lastPage = lastPage
    )

  }

  private object BookParser {

    def apply: LeioParser[LeioBook] = new BookParser

    object Keys {
      val Title = Row.Key("Title")
      val FirstPage = Row.Key("First Page")
      val LastPage = Row.Key("Last Page")
    }

  }

}

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