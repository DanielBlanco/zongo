package zongo

import mongo4cats.collection.operations._
import zio.{Chunk, RIO, Has, URLayer, ZLayer}
import zio.duration._
import zio.macros._
import zio.test._
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test.environment._
import zongo.support._
import zongo.internal.MongoRepoLive

object MongoRepoSpec extends BaseSpec {

  def spec =
    (suite("RepoMongoSpec")(tests: _*) @@ sequential)
      .provideCustomLayerShared(specLayer)

  def tests = Chunk(
    testM("find works") {
      for {
        _      <- ItemsRepo.removeAll
        _      <- ItemsRepo.insertMany(bulkInsertData)
        finder <- ItemsRepo.find
        items  <- finder.filter(inName("Luis", "John")).all
        names   = items.map(_.name)
      } yield assert(items.size)(equalTo(1)) &&
        assert(names)(equalTo(Chunk("John")))
    },
    testM("insert works") {
      for {
        _      <- ItemsRepo.removeAll
        id     <- MongoId.zmake("607ebd5d1c8f40252380ea44")
        item    = Item(Some(id), "Lorelai")
        _      <- ItemsRepo.insert(item)
        count  <- ItemsRepo.count
        finder <- ItemsRepo.find
        found  <- finder.filter(Filter.idEq(id)).first
      } yield assert(count)(equalTo(1L)) &&
        assert(found.map(_.name))(isSome(equalTo("Lorelai")))
    },
    testM("remove works") {
      for {
        _     <- ItemsRepo.removeAll
        _     <- ItemsRepo.insertMany(bulkInsertData)
        _     <- ItemsRepo.remove(inName("Daniel", "John"))
        count <- ItemsRepo.count
      } yield assert(count)(equalTo(1L))
    },
    testM("update works") {
      for {
        _      <- ItemsRepo.removeAll
        _      <- ItemsRepo.insertMany(bulkInsertData)
        u       = Update.set("name", "Daniela")
        _      <- ItemsRepo.update(byName("Daniel"), u)
        countA <- ItemsRepo.count(byName("Daniela"))
        countB <- ItemsRepo.count(byName("Daniel"))
        countC <- ItemsRepo.count(byName("John"))
      } yield assert(countA)(equalTo(1L)) &&
        assert(countB)(equalTo(0L)) &&
        assert(countC)(equalTo(1L))
    }
  )

  def byName(name: String) =
    inName(name)

  def inName(names: String*) =
    Filter.in("name", names)

  def bulkInsertData = Chunk(
    Item(Some(MongoId.make), "Daniel"),
    Item(Some(MongoId.make), "Jane"),
    Item(Some(MongoId.make), "John")
  )
}
