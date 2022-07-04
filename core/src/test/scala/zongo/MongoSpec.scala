package zongo

import mongo4cats.bson.*
import support.*
import zio.{Chunk, ZIO}
import zio.test.*
import zio.test.Assertion.*
import zio.test.TestAspect.*

object MongoSpec extends BaseSpec:

  def spec =
    (suite("MongoSpec")(tests: _*) @@ sequential)
      .provideCustomLayerShared(specLayer)

  def tests           = Chunk(
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
        _    <- ZIO.unit
        rslt <- Mongo.findCollectionNames(db)
      } yield assert(rslt)(hasSubset(collNames))
    } @@ timeout(TIMEOUT)
  )

  final val collNames = Chunk("coll_1", "coll_2")

  private def createCmd(name: String) = Document("create" -> name)

  private def dropCmd(name: String) = Document("drop" -> name)
