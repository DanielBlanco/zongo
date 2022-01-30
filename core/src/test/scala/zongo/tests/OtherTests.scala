// package zongo.tests

// import org.mongodb.scala.bson._
// import org.mongodb.scala.model.Filters
// import org.mongodb.scala.model.Indexes._
// import zio.ZIO
// import zio.duration._
// import zio.test._
// import zio.test.Assertion._
// import zio.test.TestAspect._
// import zio.test.environment._
// import zongo._
// import zongo.support._

// object OtherTests {

//   def tests = List(
//     testM("healthcheck") {
//       for {
//         db   <- Mongo.database(DB)
//         rslt <- Mongo.healthcheck(db).either
//       } yield assert(rslt)(isRight)
//     } @@ timeout(1.seconds),
//     testM("ping") {
//       for {
//         db   <- Mongo.database(DB)
//         rslt <- Mongo.ping(db).either
//       } yield assert(rslt)(isRight)
//     } @@ timeout(1.seconds),
//     testM("findCollectionNames") {
//       for {
//         db   <- Mongo.database(DB)
//         _    <- Mongo
//                   .findCollectionNames(db)
//                   .map(dropCmd)
//                   .foreach(Mongo.runCommand[Document](_)(db).runDrain)
//         _    <- Mongo.runCommand[Document](createCmd("coll_1"))(db).runDrain
//         _    <- Mongo.runCommand[Document](createCmd("coll_2"))(db).runDrain
//         rslt <- Mongo.findCollectionNames(db).runCollect
//       } yield assert(rslt)(hasSubset(Seq("coll_1", "coll_2")))
//     } @@ timeout(5.seconds),
//     test("toBsonDocument") {
//       val query    = Filters.and(Filters.eq("a", 1), Filters.eq("b", 2))
//       val result   = Mongo.toBsonDocument(query)
//       val expected =
//         Document("$and" -> BsonArray(Document("a" -> 1), Document("b" -> 2)))
//       assert(expected.toBsonDocument)(equalTo(result))
//     },
//     test("toBsonDocument converts to the same string") {
//       val query    = Filters.or(Filters.eq("a", 1), Filters.eq("b", 2))
//       val result   = Mongo.toBsonDocument(query)
//       val expected = """{"$or": [{"a": 1}, {"b": 2}]}"""
//       assert(expected)(equalTo(result.toString))
//     }
//   )

//   private def createCmd(name: String) = Document("create" -> name)

//   private def dropCmd(name: String) = Document("drop" -> name)
// }
