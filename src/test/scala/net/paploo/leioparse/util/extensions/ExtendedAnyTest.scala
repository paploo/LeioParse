package net.paploo.leioparse.util.extensions

import net.paploo.leioparse.test.TestSpec

import scala.concurrent.Promise

class ExtendedAnyTest extends TestSpec with ExtendedAny.Implicits {

  describe("tap") {

    it("should run the given side-effect on the value") {
      val p: Promise[Int] = Promise[Int]()
      val a1 = 32.tap(a => p.success(a*2))

      p.future.futureValue should === (64)
    }

    it("should return the value given") {
      val a1 = 32.tap(a => 128)

      a1 should === (32)
    }

  }

  describe("thru") {

    it("should run the given function and return the value") {
      32.thru(_*2) should === (64)
    }

  }

}
