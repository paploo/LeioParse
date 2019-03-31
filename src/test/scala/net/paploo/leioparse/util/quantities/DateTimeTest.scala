package net.paploo.leioparse.util.quantities

import java.time.{Duration, LocalDateTime, OffsetDateTime, ZoneId, ZoneOffset, ZonedDateTime}

import net.paploo.leioparse.test.SpecTest

class DateTimeTest extends SpecTest {

  describe("arithmetic") {

    val t0 = DateTime(LocalDateTime.of(2015, 10, 21, 19,28, 0))
    val t1 = DateTime(LocalDateTime.of(2015, 11, 5, 6, 0, 0))
    val ts = TimeSpan(Duration.ofHours(346).plusMinutes(32))

    it("should offset forward by a time span") {
      (t0 + ts) should === (t1)
    }

    it("should move backwards by a time span") {
      (t0 - ts) should === (DateTime(LocalDateTime.of(2015, 10, 7, 8, 56, 0)))
    }

    it("should calculate the time span between two date times") {
      (t1 - t0) should === (ts)
    }

  }

  describe("conversion") {

    val localDateTime = LocalDateTime.of(2022, 7, 16, 6,32, 24, 500000000)
    val t0 = DateTime(localDateTime)

    //I have to calculate this, since the TZ the user is running in will change the results!
    val instant = localDateTime.atZone(ZoneId.systemDefault).toInstant

    it("should give back the original java.time local date time") {
      t0.value should === (localDateTime)
    }

    it("should convert to epoch seconds as long, using the default timezone.") {
      t0.toLong should === (instant.getEpochSecond)
    }

    it("should convert to epoch seconds (in default timezone) as int") {
      t0.toInt.toLong should === (instant.getEpochSecond)
    }

    it("should convert to epoch seconds (in default timezone) as double") {
      t0.toDouble should === ((instant.toEpochMilli.toDouble / 1000.0) + 0.50)
    }

  }

  it("should order in calendar order") {
    val dt1 = DateTime(LocalDateTime.of(2015, 10, 7, 8, 56, 0))
    val dt2 = DateTime(LocalDateTime.of(2015, 10, 21, 19,28, 0))
    val dt3 = DateTime(LocalDateTime.of(2015, 11, 5, 6, 0, 0))
    val dt4 = DateTime(LocalDateTime.of(2022, 7, 16, 6,32, 24, 200000000))
    val dt5 = DateTime(LocalDateTime.of(2022, 7, 16, 6,32, 24, 300000000))
    val dt6 = DateTime(LocalDateTime.of(2022, 7, 16, 6,32, 24, 500000000))

    Seq(dt6, dt5, dt4, dt3, dt2, dt1).sorted should === (Seq(dt1, dt2, dt3, dt4, dt5, dt6))
  }

}
