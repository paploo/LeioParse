package net.paploo.leioparse

import cats.data.Reader
import net.paploo.leioparse.data.core.BookReport

package object formatter {

  /**
    * The environment to output includes the book data to output and an Outputter to write too.
    */
  case class OutputEnv(out: Outputter, reports: Seq[BookReport])

  /**
    * Formatters are functions that write a Seq[BookReport] into a Outputter, and return a value A.
    *
    * They may be composed via the Output monad, e.g. Output(formatterA)
    */
  type Formatter[+A] = OutputEnv => A

  /**
    * Output is a composable monadic form of formatters that can be run against an environment.
    *
    * Note that `FormatterComposer(formatter).run` yields back `formatter`.
    *
    * Via flatMap, formatters can be composed such that each formatter can gain access to the return value of the
    * previous formatter.
    *
    * The return value from the last formatter is the resulting value of running on an environment.
    */
  type FormatterComposer[A] = Reader[OutputEnv, A]

  object FormatterComposer {
    def apply[A](f: Formatter[A]): FormatterComposer[A] = Reader(f)
  }

}
