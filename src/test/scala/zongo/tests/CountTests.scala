package zongo.tests

import org.mongodb.scala._
import org.mongodb.scala.bson.BsonValue
import org.mongodb.scala.model.Filters
import org.mongodb.scala.model.Indexes._
import zio.ZIO
import zio.duration._
import zio.test._
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test.environment._
import zongo._
import zongo.support._

object CountTests {

  def tests = List(
    testM("count documents") {
      for {
        database   <- Mongo.database(DB)
        collection <- Mongo.collection[BsonValue](COLL_1)(database)
        result     <- Mongo.count(collection)
      } yield assert(result)(equalTo(0L))
    },
    testM("count fails if the query is not valid") {
      for {
        database   <- Mongo.database(DB)
        collection <- Mongo.collection[BsonValue](COLL_1)(database)
        result     <- Mongo.count(collection, Document("$invalid" -> "x")).either
        error       = "Command failed with error 2 (BadValue)"
      } yield assert(result)(isLeft(hasMessage(containsString(error))))
    }
    // testM("count returns the number of records in the collection") {
    //   for {
    //     a <- Mongo.count(COLL_1)
    //     _ <- Mongo.insert(COLL_1, Bson.obj("_id" -> OBJ_ID_1, "name" -> "A"))
    //     b <- Mongo.count(COLL_1)
    //     _ <- Mongo.clearCollection(COLL_1)
    //   } yield assert((a, b))(equalTo((0L, 1L)))
    // },
    // testM("count returns the number of filtered records in the collection") {
    //   for {
    //     a <- Mongo.count(COLL_2)
    //     _ <- Mongo.bulkInsert(COLL_2, bulkInsertData)
    //     b <- Mongo.count(COLL_2)
    //     c <- Mongo.count(COLL_2, Filters.in("num", 2, 3))
    //     _ <- Mongo.clearCollection(COLL_2)
    //   } yield assert((a, b, c))(equalTo((0L, 3L, 2L)))
    // }
  )
}
