package net.paploo.leioparse.util.quantities

import net.paploo.leioparse.test.TestSpec

class BlocksTest extends TestSpec {

  describe("arithmetic") {

    describe("multiply by ratio") {

      it("should scale the blocks by the given ratio, rounded to the nearest int") {
        (Blocks(10) * Ratio(1.6667)) should === (Blocks(17))
      }

    }

    describe("multiply by word density") {

      it("should multiply to words using a density, rounded to the nearest int") {
        (Blocks(10) * WordDensity(66.66667)) should === (Words(667))
      }

    }

  }

}
