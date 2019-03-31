package net.paploo.leioparse.util.quantities

import java.time.Duration

import net.paploo.leioparse.test.TestSpec

class BlockRateTest extends TestSpec {

  describe("arithmetic") {

    it("should calculate the correct value in blocks/hour from blocks and a time span") {
      BlockRate.from(Blocks(120), TimeSpan(Duration.ofHours(2))) should === (BlockRate(60.0))
    }

    it("should calcualte the correct block pace in minutes per block") {
      BlockRate(40).inverse should === (BlockPace(1.5))
    }

  }

}
