// package zongo.tests

// import org.mongodb.scala._
// import org.mongodb.scala.bson.BsonString
// import org.mongodb.scala.model.{Filters, IndexModel, Updates}
// import org.mongodb.scala.model.Indexes._
// import zio.ZIO
// import zio.duration._
// import zio.test._
// import zio.test.Assertion._
// import zio.test.TestAspect._
// import zio.test.environment._
// import zongo._
// import zongo.support._

// object IndexesTests {

//   def tests = List(
//     testM("create an index by one field") {
//       for {
//         c <- collection1
//         a <- Mongo.createIndex(c, ascending("name"))
//       } yield assert(a)(equalTo("name_1"))
//     },
//     testM("create an index by multiple fields") {
//       for {
//         c <- collection1
//         a <- Mongo.createIndex(c, ascending("name", "lastName"))
//       } yield assert(a)(equalTo("name_1_lastName_1"))
//     },
//     testM("create a compound index") {
//       for {
//         c <- collection1
//         a <- Mongo.createIndex(
//                c,
//                compoundIndex(ascending("name"), descending("lastName"))
//              )
//       } yield assert(a)(equalTo("name_1_lastName_-1"))
//     },
//     testM("create multiple indexes") {
//       for {
//         c        <- collection1
//         indexList = Seq(
//                       IndexModel(ascending("name")),
//                       IndexModel(ascending("name", "lastName")),
//                       IndexModel(
//                         compoundIndex(ascending("name"), descending("lastName"))
//                       )
//                     )
//         indices  <- Mongo.createIndexes(c, indexList).runCollect
//       } yield assert(indices.length)(equalTo(3)) &&
//         assert(indices(0))(equalTo("name_1")) &&
//         assert(indices(1))(equalTo("name_1_lastName_1")) &&
//         assert(indices(2))(equalTo("name_1_lastName_-1"))
//     },
//     testM("listIndexes retrieves all indexes in a collection") {
//       for {
//         c        <- collection1
//         indexList = Seq(
//                       IndexModel(ascending("name")),
//                       IndexModel(ascending("name", "lastName")),
//                       IndexModel(
//                         compoundIndex(ascending("name"), descending("lastName"))
//                       )
//                     )
//         a        <- Mongo.createIndexes(c, indexList).runCollect
//         b        <- Mongo.listIndexes(c).runCollect
//         names     = b.map(x => x[BsonString]("name")).map(_.getValue)
//       } yield assert(names)(contains("name_1")) &&
//         assert(names)(contains("name_1_lastName_1")) &&
//         assert(names)(contains("name_1_lastName_-1"))
//     }
//   )
// }
