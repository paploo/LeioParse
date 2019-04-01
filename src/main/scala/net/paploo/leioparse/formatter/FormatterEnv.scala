package net.paploo.leioparse.formatter

import net.paploo.leioparse.data.core.BookReport

/**
  * The environment to output includes the book data to output and an Outputter to write too.
  */
case class FormatterEnv(out: Outputter, reports: Seq[BookReport])