package net.paploo.leioparse.util.quantities

import net.paploo.leioparse.test.TestSpec

class LocationTest extends TestSpec {

  describe("arithmetic") {

    describe("to") {

      it("should calculate the range span inclusively") {
        (Location(1) to Location(10)) should === (Blocks(10))
      }

    }

    describe("until") {

      it("should calculate the range span exclusive of the end bound") {
        (Location(1) until Location(10)) should === (Blocks(9))
      }

    }

    describe("ratio") {

      it("shoudl calculate the ratio of the ranges") {
        (Location(50) / Location(100)) should === (Ratio(0.50))
      }

    }

  }

  describe("value extraction") {

    it("should return the int value") {
      Location(549).value should === (549)
      Location(549).toInt should === (549)
    }

  }

  describe(s"ordering") {

    it("should order by location value") {
      val locations = Seq(623, 563, 122, 4678, 1, 53456).map(Location.apply)
      locations.sorted.map(_.value) should === (locations.map(_.value).sorted)
    }

  }

}
