package net.paploo.leioparse

import cats.data.Reader
import net.paploo.leioparse.data.core.BookSessions

package object outputterformatter {

  /**
    * The environment to output includes the book data to output and an Outputter to write too.
    */
  case class OutputEnv(out: Outputter, books: Seq[BookSessions])

  /**
    * Formatters are functions that write a Seq[BookSessions] into a Outputter, and return a value A.
    */
  type Formatter[+A] = OutputEnv => A

  /**
    * Output is a composable monadic form of formatters that can be run against an environment.
    *
    * Via flatMap, each formatter can gain access to the return value of the previous formatter.
    *
    * The return value from the last formatter is the resulting value of running on an environment.
    */
  type Output[A] = Reader[OutputEnv, A]

  object Output {
    def apply[A](f: Formatter[A]): Output[A] = Reader(f)
  }

}
