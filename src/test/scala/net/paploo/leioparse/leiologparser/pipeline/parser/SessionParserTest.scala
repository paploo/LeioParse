package net.paploo.leioparse.leiologparser.pipeline.parser

import java.time.{Duration, LocalDateTime}

import net.paploo.leioparse.leiologparser.pipeline.LeioParsePipeline.Row
import net.paploo.leioparse.leiologparser.pipeline.LeioParsePipeline.Row.{Key, Value}
import net.paploo.leioparse.test.TestSpec
import net.paploo.leioparse.util.quantities.{DateTime, TimeSpan}

import scala.util.{Failure, Success}

class SessionParserTest extends TestSpec {

  describe(s"backdating") {

    val parser = SessionParser()

    it("should use the read start/end/duration when present") {
      //row is plucked from live input!
      val row = Row(Map(Key("Time/Page") -> Some(Value("1 min 25 s")), Key("Last Page") -> Some(Value("103")), Key("Started On") -> Some(Value("8/30/18 20:55")), Key("Book") -> Some(Value("Relic Worlds 1")), Key("Finished On") -> Some(Value("8/30/18 21:29")), Key("Pages Read") -> Some(Value("24")), Key("Duration") -> Some(Value("34 min 7 s")), Key("First Page") -> Some(Value("80"))))
      val parsed = parser(row)
      parsed match {
        case Success(session) =>
          session.startedOn should === (DateTime(LocalDateTime.of(2018,8,30,20,55,0)))
          session.finishedOn should === (DateTime(LocalDateTime.of(2018,8,30,21,29,0)))
          session.duration should === (TimeSpan(Duration.ofMinutes(34).plusSeconds(7)))
        case Failure(th) =>
          fail("Expected to be able to parse row but got exception $th")
      }
    }

    //This happens on some manually entered sessions
    it("should back-date a missing start date when a start date is missing") {
      //row is plucked from live input!
      val row = Row(Map(Key("Time/Page") -> Some(Value("1 min 42 s")), Key("Last Page") -> Some(Value("63")), Key("Started On") -> None, Key("Book") -> Some(Value("Relic Worlds 1")), Key("Finished On") -> Some(Value("8/24/18 14:00")), Key("Pages Read") -> Some(Value("63")), Key("Duration") -> Some(Value("1 h 48 min")), Key("First Page") -> Some(Value("1"))))
      val parsed = parser(row)
      parsed match {
        case Success(session) =>
          session.startedOn should === (DateTime(LocalDateTime.of(2018,8,24,12,12,0)))
          session.finishedOn should === (DateTime(LocalDateTime.of(2018,8,24,14,0,0)))
          session.duration should === (TimeSpan(Duration.ofHours(1).plusMinutes(48)))
        case Failure(th) =>
          fail("Expected to be able to parse row but got exception $th")
      }
    }

  }

}
