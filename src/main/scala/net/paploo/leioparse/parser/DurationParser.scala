package net.paploo.leioparse.parser

import java.time.Duration

import scala.util.matching.Regex

object DurationParser {

  private[this] val leioPattern: Regex ="""((\d+) h)?\s*((\d+) min)\s*?((\d+) s)?""".r

  def leio: String => Duration = {
    case leioPattern(_, h, _, m, _, s) =>
      Duration.ofHours(
        Option(h).map(_.toLong).getOrElse(0L)
      ).plusMinutes(
        Option(m).map(_.toLong).getOrElse(0L)
      ).plusSeconds(
        Option(s).map(_.toLong).getOrElse(0L)
      )
  }

}
