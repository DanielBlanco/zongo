// package zongo

// import org.mongodb.scala._
// import org.mongodb.scala.model.{Filters, Updates}
// import zio.{Chunk, RIO, Has, URLayer, ZLayer}
// import zio.duration._
// import zio.macros._
// import zio.test._
// import zio.test.Assertion._
// import zio.test.TestAspect._
// import zio.test.environment._
// import zongo.support._
// import zongo.internal.MongoRepoLive

// object MongoRepoSpec extends BaseSpec {

//   def spec =
//     (suite("RepoMongoSpec")(tests: _*) @@ sequential)
//       .provideCustomLayerShared(specLayer)

//   def tests = List(
//     testM("find works") {
//       for {
//         _     <- ItemsRepo.removeAll
//         _     <- ItemsRepo.insertMany(bulkInsertData)
//         items <- ItemsRepo.find(inName("Luis", "John")).runCollect
//         names  = items.map(_.name)
//       } yield assert(items.size)(equalTo(1)) &&
//         assert(names)(equalTo(Chunk("John")))
//     },
//     testM("remove works") {
//       for {
//         _     <- ItemsRepo.removeAll
//         _     <- ItemsRepo.insertMany(bulkInsertData)
//         _     <- ItemsRepo.remove(inName("Daniel", "John"))
//         count <- ItemsRepo.count
//       } yield assert(count)(equalTo(1L))
//     },
//     testM("update works") {
//       for {
//         _      <- ItemsRepo.removeAll
//         _      <- ItemsRepo.insertMany(bulkInsertData)
//         u       = Updates.set("name", "Daniela")
//         _      <- ItemsRepo.update(byName("Daniel"), u)
//         countA <- ItemsRepo.count(byName("Daniela"))
//         countB <- ItemsRepo.count(byName("Daniel"))
//         countC <- ItemsRepo.count(byName("John"))
//       } yield assert(countA)(equalTo(1L)) &&
//         assert(countB)(equalTo(0L)) &&
//         assert(countC)(equalTo(1L))
//     }
//   )

//   def byName(name: String) =
//     Filters.in("name", name)

//   def inName(names: String*) =
//     Filters.in("name", names: _*)

//   def bulkInsertData = Seq(
//     Item(Some(MongoId.make), "Daniel"),
//     Item(Some(MongoId.make), "Jane"),
//     Item(Some(MongoId.make), "John")
//   )
// }
