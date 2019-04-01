package net.paploo.leioparse

import cats.data.Reader

package object formatter {

  /**
    * Formatters are functions that write a Seq[BookReport] into a Outputter, and return a value A.
    *
    * They may be composed via the Output monad, e.g. Output(formatterA)
    */
  type Formatter[+A] = FormatterEnv => A

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
  type FormatterComposer[A] = Reader[FormatterEnv, A]

  object FormatterComposer {
    def apply[A](f: Formatter[A]): FormatterComposer[A] = Reader(f)
  }

}
