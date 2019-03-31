package net.paploo.leioparse.util.quantities

import java.time.Duration

import net.paploo.leioparse.test.TestSpec

class BlockPaceTest extends TestSpec {

  describe("arithmetic") {

    it("should calculate the correct value in blocks/hour from blocks and a time span") {
      BlockPace.from(TimeSpan(Duration.ofHours(2)), Blocks(120)) should === (BlockPace(1.0))
    }

    it("should calculate the correct block pace in minutes per block") {
      BlockPace(1.5).inverse should === (BlockRate(40))
    }

  }

}
