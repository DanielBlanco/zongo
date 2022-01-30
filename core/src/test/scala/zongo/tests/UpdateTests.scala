// package zongo.tests

// import org.bson.BsonString
// import org.mongodb.scala.model.{Filters, Updates}
// import org.mongodb.scala.model.Indexes._
// import zio.duration._
// import zio.test._
// import zio.test.Assertion._
// import zio.test.TestAspect._
// import zio.test.environment._
// import zongo._
// import zongo.support._

// object UpdateTests {

//   def tests = List(
//     testM("update finds and updates the record") {
//       for {
//         c       <- collection2
//         _       <- Mongo.insertMany(c, bulkInsertData)
//         query    = Filters.eq("num", 2)
//         modifier = Updates.set("num", 22)
//         rslt    <- Mongo.update(c, query, modifier)
//         (1, a)   = (rslt.getMatchedCount(), rslt.getModifiedCount())
//         b       <- Mongo.find(c, Filters.eq("num", 22)).runCollect
//         _       <- Mongo.clearCollection(c)
//         c        = b.map(_.get("num").get.asNumber.intValue).head
//       } yield assert(a)(equalTo(1L)) && assert(c)(equalTo(22))
//     },
//     testM("update return tuple with found and updated count") {
//       for {
//         c               <- collection2
//         _               <- Mongo.insertMany(c, bulkInsertData)
//         query            = Filters.eq("num", 2)
//         modifier         = Updates.set("num", 2)
//         rslt            <- Mongo.update(c, query, modifier)
//         (found, updated) = (rslt.getMatchedCount(), rslt.getModifiedCount())
//         _               <- Mongo.clearCollection(c)
//       } yield assert(found)(equalTo(1L)) && assert(updated)(equalTo(0L))
//     }
//   )

// }
