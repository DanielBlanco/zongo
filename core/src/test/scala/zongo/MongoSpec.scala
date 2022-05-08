package zongo

import mongo4cats.bson.*
import support.*
import zio.{Chunk, ZIO}
import zio.test.*
import zio.test.Assertion.*
import zio.test.TestAspect.*

object MongoSpec extends BaseSpec {

  def spec = // @@ before(clearDB)
    (suite("MongoSpec")(tests: _*) @@ sequential)
      .provideCustomLayerShared(specLayer)

  // CountTests.tests ++
  //   IndexesTests.tests ++
  //   FindTests.tests ++
  //   UpdateTests.tests ++
  //   OtherTests.tests
  def tests           = List(
    test("healthcheck") {
      for {
        db   <- Mongo.getDatabase(TEST_DB)
        rslt <- Mongo.healthcheck(db).either
      } yield assert(rslt)(isRight)
    } @@ timeout(TIMEOUT),
    test("ping") {
      for {
        db   <- Mongo.getDatabase(TEST_DB)
        rslt <- Mongo.ping(db).either
      } yield assert(rslt)(isRight)
    } @@ timeout(TIMEOUT),
    test("findCollectionNames") {
      for {
        db   <- Mongo.getDatabase(TEST_DB)
        old  <- Mongo.getCollections(collNames)(db)
        _    <- Mongo.dropCollections(old)
        _    <- Mongo.createCollections(collNames)(db)
        rslt <- Mongo.findCollectionNames(db)
      } yield assert(rslt)(hasSubset(collNames))
    } @@ timeout(TIMEOUT)
  )

  final val collNames = Chunk("coll_1", "coll_2")

  private def createCmd(name: String) = Document("create" -> name)

  private def dropCmd(name: String) = Document("drop" -> name)
}
