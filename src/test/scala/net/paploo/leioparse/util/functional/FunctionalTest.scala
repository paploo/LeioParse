package net.paploo.leioparse.util.functional

import net.paploo.leioparse.test.TestSpec

import scala.concurrent.Promise

class FunctionalTest extends TestSpec with Functional {

  describe("id") {

    it("should evaluate to whatever is passed in") {
      val obj = new Object
      id(obj) should === (obj)
    }

  }

  describe("const") {

    it("should return a type of value b no matter what is passed in") {
      val f = const(88)
      f(1039) should === (88)
      f("asdf") should === (88)
      f(new Object) should === (88)
    }

    it("should be useful in a map") {
      Option(new Object).map(const(88)) should === (Some(88))
    }

  }

  describe("tap") {

    it("should return what it was passed, regardless of what the function passes out") {
      val obj = new Object
      tap[Object, String](_ => "Flux Capacitor")(obj) should === (obj)
    }

    it("should run a side-effect on the value") {
      val promise: Promise[Int] = Promise[Int]()
      tap[Int, Unit](a => promise.success(a))(88)
      promise.future.futureValue should === (88)
    }

    it("should be useful in a map") {
      val promise: Promise[Int] = Promise[Int]()
      val result = Option(88).map(tap(a => promise.success(a)))
      result should === (Some(88))
      promise.future.futureValue should === (88)
    }

  }

  describe("swap") {

    it("should swap the argument order") {

      val f: String => Int => Option[String] = s => i => Option(s*i)
      val swapped: Int => String => Option[String] = swap(f)

      swapped(3)("zap") should === (f("zap")(3))

    }

  }

}
