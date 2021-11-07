package zongo

import mongo4cats.collection.operations.*
import zio.{Chunk, RIO, Has, URLayer, ZLayer, ZIO}
import zio.duration.*
import zio.macros.*
import zio.test.*
import zio.test.Assertion.*
import zio.test.TestAspect.*
import zio.test.environment.*
import zongo.support.*

object MongoRepoSpec extends BaseSpec {

  def spec =
    (suite("RepoMongoSpec")(tests: _*) @@ sequential)
      .provideCustomLayerShared(specLayer)

  def tests = Chunk(
    testM("find works") {
      for {
        _     <- ItemsRepo.removeAll
        _     <- ItemsRepo.insertMany(bulkInsertData)
        items <- ItemsRepo.findChunks(inName("Luis", "John"))
        names  = items.map(_.name)
      } yield assert(items.size)(equalTo(1)) &&
        assert(names)(equalTo(Chunk("John")))
    },
    testM("insert works") {
      for {
        _     <- ItemsRepo.removeAll
        id    <- MongoId.zmake("607ebd5d1c8f40252380ea44")
        item   = Item(Some(id), "Lorelai")
        _     <- ItemsRepo.insert(item)
        count <- ItemsRepo.count
        found <- ItemsRepo.findFirst(Filter.idEq(id))
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
    },
    testM("update document works") {
      for {
        _      <- ItemsRepo.removeAll
        _      <- ItemsRepo.insertMany(bulkInsertData)
        finder <- ItemsRepo.finder
        docOpt <- finder.filter(byName("Daniel")).first
        doc    <- ZIO.fromOption(docOpt)
        doc2    = doc.copy(name = "Daniela")
        _      <- ItemsRepo.update(doc2)
        countA <- ItemsRepo.count(byName("Daniela"))
        countB <- ItemsRepo.count(byName("Daniel"))
      } yield assert(countA)(equalTo(1L)) &&
        assert(countB)(equalTo(0L))
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
