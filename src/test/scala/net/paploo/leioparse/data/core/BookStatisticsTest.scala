package net.paploo.leioparse.data.core

import java.time.{Duration, LocalDateTime}

import net.paploo.leioparse.data.core.BookStatistics.StatisticsComputationException
import net.paploo.leioparse.data.core.BookStatisticsTest.Fixtures
import net.paploo.leioparse.test.TestSpec
import net.paploo.leioparse.util.quantities._

import scala.util.{Failure, Success, Try}

class BookStatisticsTest extends TestSpec {

  describe("from") {

    describe("book with no sessions") {

      it("should return a failure with a StatisticsComputationException") {
        val statsTry = BookStatistics.from(Fixtures.book, Seq.empty)
        statsTry match {
          case Success(a) => fail(s"Expected a Failure but got a Success($a)")
          case Failure(th) => th shouldBe a[StatisticsComputationException]
        }
      }

    }
    
    describe("well formed data") {
      
      describe("calendar date stats") {

        it("should use the earliest session start date") {
          Fixtures.statsTryWithUnorderedSessions.map(_.calendarDateStats.start) should === (Success(DateTime(LocalDateTime.parse("2019-02-01T12:00"))))
        }

        it("should use the latest session end date") {
          Fixtures.statsTryWithUnorderedSessions.map(_.calendarDateStats.last) should === (Success(DateTime(LocalDateTime.parse("2019-02-07T12:00"))))
        }

      }
      
      describe("location stats") {

        it("should use the smallest seen location for the start") {
          Fixtures.statsTryWithUnorderedSessions.map(_.locationStats.start) should === (Success(Location(10)))
        }

        it("should use the largest seen location for the end") {
          Fixtures.statsTryWithUnorderedSessions.map(_.locationStats.last) should === (Success(Location(84)))
        }

      }
      
      describe("progress") {

        it("should give the completed percentage as a ratio") {
          Fixtures.statsTry.map(_.progress.completed) should === (Success(Ratio(0.75)))
        }

        it("should give return the total blocks read") {
          Fixtures.statsTry.map(_.progress.blocksRead) should === (Success(Blocks(75)))
        }

        it("should give the block total block remaining") {
          Fixtures.statsTry.map(_.progress.blocksRemaining) should === (Success(Blocks(25)))
        }

        it("should give the total words read") {
          Fixtures.statsTry.map(_.progress.wordsRead) should === (Success(Words(75 * 300)))
        }

        it("should give the total words remaining") {
          Fixtures.statsTry.map(_.progress.wordsRemaining) should === (Success(Words(25 * 300)))
        }

        it("should give the total of the session durations") {
          Fixtures.statsTry.map(_.progress.cumulativeReadingTime) should === (Success(TimeSpan(Duration.parse("PT3H"))))
        }

        it("should give the total time elapsed from first opening the book until the last time it was closed") {
          Fixtures.statsTry.map(_.progress.calendarDuration) should === (Success(TimeSpan(Duration.parse("P6D"))))
        }
        
      }
      
      describe("session reading rates") {

        it("should give the reading speed (block rate) in blocks per hour") {
          Fixtures.statsTry.map(_.sessionReadingRates.blockRate) should === (Success(BlockRate(25.0)))
        }

        it("should give the reading pace (block pace) in minutes per block") {
          Fixtures.statsTry.map(_.sessionReadingRates.blockPace) should === (Success(BlockPace(2.4)))
        }

        it("should give the word reading rate in words per minute") {
          Fixtures.statsTry.map(_.sessionReadingRates.wordRate) should === (Success(WordRate(125.0)))
        }
        
      }
      
      describe("book reading rates") {

        it("should give the average number of blocks read per calendar day (using the total time elapsed for reading the book, not just the total session time") {
          Fixtures.statsTry.map(_.bookReadingRates.blockDailyRate) should === (Success(BlockDailyRate(12.5)))
        }
        
      }
      
      describe("estimates") {

        it("should give the ETR, being the duration one would need to read to complete the book based on the average speed") {
          Fixtures.statsTry.map(_.estimates.timeRemaining) should === (Success(TimeSpan(Duration.parse("PT1H"))))
        }

        it("should give the estimated number of days until completion, at the average daily session length") {
          Fixtures.statsTry.map(_.estimates.calendarDaysRemaining) should === (Success(TimeSpan(Duration.parse("P2D"))))
        }

        it("should give the estimated date of completion") {
          Fixtures.statsTry.map(_.estimates.completionDate) should === (Success(DateTime(LocalDateTime.parse("2019-02-09T12:00"))))
        }

      }

      describe("cumulativeSessionStatistics") {

        it("should have the same length as the sessions") {
          Fixtures.statsTry.map(_.cumulativeSessionStatistics.length) should === (Success(Fixtures.sessions.length))
        }

        it("should calculate the cumulative blocks read at each session") {
          Fixtures.statsTry.map(_.cumulativeSessionStatistics.map(_.blocks)) should === (Success(Seq(
            Blocks(10),
            Blocks(35),
            Blocks(40),
            Blocks(75)
          )))
        }

        it("should calculate the cumulative words read at each session") {
          //Should be same as for blocks, but the value should be bigger by the book average word density
          Fixtures.statsTry.map(_.cumulativeSessionStatistics.map(_.words)) should === (Success(Seq(
            Words(10*300),
            Words(35*300),
            Words(40*300),
            Words(75*300)
          )))
        }

        it("should calculate the cumulative completion progress at each session") {
          Fixtures.statsTry.map(_.cumulativeSessionStatistics.map(_.completed)) should === (Success(Seq(
            Ratio(10.0/100.0),
            Ratio(35.0/100.0),
            Ratio(40.0/100.0),
            Ratio(75.0/100.0)
          )))
        }

        it("should calculate the cumulative session duration at each session") {
          Fixtures.statsTry.map(_.cumulativeSessionStatistics.map(_.duration)) should === (Success(Seq(
            TimeSpan(Duration.parse("PT30M")),
            TimeSpan(Duration.parse("PT1H30M")),
            TimeSpan(Duration.parse("PT1H40M")),
            TimeSpan(Duration.parse("PT3H"))
          )))
        }

        it("should calculate the cumulative calendar duration at each session") {
          Fixtures.statsTry.map(_.cumulativeSessionStatistics.map(_.calendarDuration)) should === (Success(Seq(
            TimeSpan(Duration.parse("PT30M")), //This should be the length of the first session!
            TimeSpan(Duration.parse("P2DT12H30M")),
            TimeSpan(Duration.parse("P3DT3H10M")),
            TimeSpan(Duration.parse("P6D"))
          )))
        }

      }
      
    }

    describe("malformed data") {

      it("should return zero for the block rate and block pace if the cumulative reading time is zero") {
        val sessions = Fixtures.sessions.map(_.copy(duration = TimeSpan.Zero, startLocation = Location(10), endLocation = Location(10)))
        val bookTry = BookStatistics.from(Fixtures.book, sessions)
        bookTry.map(_.sessionReadingRates.blockRate) should === (Success(BlockRate(0.0)))
        bookTry.map(_.sessionReadingRates.blockPace) should === (Success(BlockPace(0.0)))
      }

    }

  }

}

object BookStatisticsTest {

  object Fixtures {

    def statsTryWithUnorderedSessions: Try[BookStatistics] = BookStatistics.from(book, sessions.sortBy(_.duration))

    def statsTry: Try[BookStatistics] = BookStatistics.from(book, sessions)

    val book: Book = Book(title = Book.Title("Back in Time"),
                          startLocation = Location(10), //page 10
                          endLocation = Location(109), //page 10 is the first, so 100 full pages means we end on 109.
                          averageWordDensity = WordDensity(300), //words per page
                          externalId = None)

    // 100 locations in 4 hours = 25 locs/hour or 2.4 min/loc.
    // 75 locations (10 to 84) in 3 hours is same rate.
    // Also, picked last start time so the calendar time would be 75 blocks in 6 days.
    val sessions: Seq[Session] = Seq(
      Session(book.title, DateTime(LocalDateTime.parse("2019-02-01T12:00")), TimeSpan(Duration.parse("PT30M")), Location(10), Location(19)),
      Session(book.title, DateTime(LocalDateTime.parse("2019-02-03T23:30")), TimeSpan(Duration.parse("PT1H")), Location(20), Location(44)),
      Session(book.title, DateTime(LocalDateTime.parse("2019-02-04T15:00")), TimeSpan(Duration.parse("PT10M")), Location(45), Location(49)),
      Session(book.title, DateTime(LocalDateTime.parse("2019-02-07T10:40")), TimeSpan(Duration.parse("PT1H20M")), Location(50), Location(84))
    )

  }

}