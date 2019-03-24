package net.paploo.leioparse.leiologparser.pipeline

import net.paploo.leioparse.data.leiofile.{LeioBook, LeioSession}
import net.paploo.leioparse.leiologparser.pipeline.LeioParsePipeline.Row
import net.paploo.leioparse.leiologparser.pipeline.parser.ValueParser

import scala.util.{Failure, Success, Try}

trait LeioParser[A] extends (Row => Try[A])

object LeioParser {

  val SessionParser: LeioParser[LeioSession] = parser.SessionParser()

  val BookParser: LeioParser[LeioBook] = parser.BookParser()

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

}