package net.paploo.leioparse.data.core

import net.paploo.leioparse.test.TestSpec

class BookStatisticsTest extends TestSpec {

  describe("from") {

    describe("book with no sessions") {

      it("should return a failure with a StatisticsComputationException") {
        pending
      }

    }
    
    describe("well formed data") {
      
      describe("calendar date stats") {

        it("should use the earliest session start date") {
          pending
        }

        it("should use the latest session end date") {
          pending
        }

      }
      
      describe("location stats") {

        it("should use the smallest seen location for the start") {
          pending
        }

        it("should use the largest seen location for the end") {
          pending
        }

      }
      
      describe("progress") {

        it("should give the completed percentage as a ratio") {
          pending
        }

        it("should give return the total blocks read") {
          pending
        }

        it("should give the block total block remaining") {
          pending
        }

        it("should give the total words read") {
          pending
        }

        it("should give the total words remaining") {
          pending
        }

        it("should give the total of the session durations") {
          pending
        }

        it("should give the total time elapsed from first opening the book until the last time ti was closed") {
          pending
        }
        
      }
      
      describe("session reading rates") {

        it("should give the reading speed (block rate) in blocks per hour") {
          pending
        }

        it("should give the reading pace (block pace) in minutes per block") {
          pending
        }

        it("should give the word reading rate in words per minute") {
          pending
        }
        
      }
      
      describe("book reading rates") {

        it("should give the average number of blocks read per calendar day (using the total time elapsed for reading the book, not just the total session time") {
          pending
        }
        
      }
      
      describe("estimates") {

        it("should give the ETR, being the duration one would need to read to complete the book based on the average speed") {
          pending
        }

        it("should give the estimated number of days until completion, at the average daily session length") {
          pending
        }

        it("should give the estimated date of completion") {
          pending
        }

      }
      
    }

    describe("malformed data") {

      it("should return zero for the completed amount if the book length is zero") {
        pending
      }

      it("should return zero for the block rate and block pace if the cumulative reading time is zero") {
        pending
      }

    }

  }

}
