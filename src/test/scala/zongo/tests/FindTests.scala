package zongo.tests

import org.mongodb.scala._
import org.mongodb.scala.bson.BsonValue
import org.mongodb.scala.model.Filters
import org.mongodb.scala.model.Sorts
import zio.Chunk
import zio.duration._
import zio.test._
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test.environment._
import zongo._
import zongo.support._

object FindTests {

  def tests = List(
    testM("find allows filtering") {
      for {
        c    <- collection2
        _    <- Mongo.insertMany(c, bulkInsertData)
        q     = Filters.in("num", 2, 3)
        docs <- Mongo.find(c, q).runCollect
        _    <- Mongo.clearCollection(c)
        nums  = docs.map(_.get("num").get.asNumber.intValue)
      } yield assert(docs.size)(equalTo(2)) && assert(nums)(equalTo(Chunk(2, 3)))
    },
    testM("find allows sorting") {
      for {
        c    <- collection2
        _    <- Mongo.insertMany(c, bulkInsertData)
        q     = Filters.in("num", 1, 2)
        s     = Some(Sorts.descending("num"))
        docs <- Mongo.find(c, q, s).runCollect
        _    <- Mongo.clearCollection(c)
        nums  = docs.map(_.get("num").get.asNumber.intValue)
      } yield assert(nums)(equalTo(Chunk(2, 1)))
    },
    testM("find allows limiting the results") {
      for {
        c    <- collection2
        _    <- Mongo.insertMany(c, bulkInsertData)
        q     = Filters.in("num", 1, 2)
        s     = Some(Sorts.descending("num"))
        docs <- Mongo.find(c, q, s, Some(1)).runCollect
        _    <- Mongo.clearCollection(c)
        nums  = docs.map(_.get("num").get.asNumber.intValue)
      } yield assert(docs.size)(equalTo(1)) &&
        assert(nums)(equalTo(Chunk(2)))
    },
    testM("distinct") {
      for {
        c    <- collection2
        _    <- Mongo.insertMany(c, bulkInsertData)
        q     = Filters.eq("g", 1)
        docs <- Mongo.distinct(c, "num", q).runCollect
        _    <- Mongo.clearCollection(c)
        nums  = docs.map(_.asNumber.intValue)
      } yield assert(nums)(equalTo(Chunk(1, 2, 3)))
    }
  )

}
