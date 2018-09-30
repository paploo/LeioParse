package net.paploo.leioparse.parser

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateParser {

  def standard: String => LocalDateTime = s => LocalDateTime.parse(s, DateTimeFormatter.ofPattern("M/d/yy HH:mm"))

}
