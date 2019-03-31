package net.paploo.leioparse.util.extensions

import net.paploo.leioparse.test.TestSpec

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class ExtendedFutureTest extends TestSpec with ExtendedFuture.Implicits {

  implicit val ec: ExecutionContext = ExecutionContext.global

  describe("toTry") {

    it("should convert a success to a success") {
      Future(3).toTry should === (Success(3))
    }

    it("should convert a failure to the failure") {
      val t = Future(1/0).toTry
      t shouldBe 'failure
      t.failed.get shouldBe a[ArithmeticException]
    }

  }

}
