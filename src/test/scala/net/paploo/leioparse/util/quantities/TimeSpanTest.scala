package net.paploo.leioparse.util.quantities

import java.time.Duration

import net.paploo.leioparse.test.SpecTest

class TimeSpanTest extends SpecTest {

  describe("arithmetic") {

    describe("addition") {

      it("should add positive time spans") {
        val sum = TimeSpan(Duration.ofMinutes(45)) + TimeSpan(Duration.ofMinutes(30))
        sum should === (TimeSpan(Duration.ofMinutes(75)))
      }

      it("should be identity over zero") {
        val ts = TimeSpan(Duration.ofMinutes(45))

        (ts + TimeSpan.Zero) should === (ts)
        (TimeSpan.Zero + ts) should === (ts)
      }

    }

    describe("scaling") {

      it("should scale by a ratio > 1") {
        val product = TimeSpan(Duration.ofMinutes(30)) * Ratio(2.5)
        product should === (TimeSpan(Duration.ofMinutes(75)))
      }

      it("should scale by a ratio > 0 && < 1") {
        val product = TimeSpan(Duration.ofMinutes(40)) * Ratio(0.25)
        product should === (TimeSpan(Duration.ofMinutes(10)))
      }

    }

  }

  describe("getting values") {

    val ts = TimeSpan(Duration.ofMinutes(45))

    it("should return the value as a java.time duration") {
      ts.value should === (Duration.ofMinutes(45))
    }

    it("should compute total integral seconds") {
      ts.toInt should === (2700)
      ts.toSeconds should === (2700)
    }

    it("should compute total fractional seconds") {
      ts.toDouble should === (2700.0)
    }

    it("should compute total fractional minutes") {
      ts.toMinutes should === (45.0)
    }

    it("should compute total fractional hours") {
      ts.toHours should === (0.75)
    }

    it("should compute total fractional days") {
      ts.toDays should === (0.03125)
    }

  }

  describe("orering") {

    it("should implicitly order time spans") {
      val tsA =  TimeSpan(Duration.ofSeconds(1))
      val tsB =  TimeSpan(Duration.ofHours(1))
      val tsC =  TimeSpan(Duration.ofMinutes(75))
      val tsD =  TimeSpan(Duration.ofSeconds(86400))

      Seq(tsD, tsB, tsC, tsA).sorted should === (Seq(tsA, tsB, tsC, tsD))
    }

  }

}
