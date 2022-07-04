package zongo

import mongo4cats.collection.operations.*
import com.mongodb.client.model.Filters
import java.util.UUID
import zio.*
import zio.test.*
import zio.test.Assertion.*
import zio.test.TestAspect.*
import zongo.support.*

object MongoRepoSpec extends BaseSpec:

  def spec =
    (suite("RepoMongoSpec")(tests: _*) @@ sequential)
      .provideCustomLayerShared(specLayer)

  def tests = Chunk(
    test("explain works") {
      for {
        _    <- ItemsRepo.removeAll
        _    <- ItemsRepo.insertMany(bulkInsertData)
        // query <- ItemsRepo.translate(Filter.eq("name", "Luis"))
        expl <- ItemsRepo.explain(inName("Luis", "John"))
        xpct  = "filter=Document{{name=Document{{$in=[John, Luis]}}}}"
      } yield assert(expl.toString)(containsString(xpct))
    },
    test("find works") {
      for {
        _     <- ItemsRepo.removeAll
        _     <- ItemsRepo.insertMany(bulkInsertData)
        items <- ItemsRepo.findChunks(inName("Luis", "John"))
        names  = items.map(_.name)
      } yield assert(items.size)(equalTo(1)) &&
        assert(names)(equalTo(Chunk("John")))
    },
    test("find by UUID works") {
      for {
        _       <- ItemsRepo.removeAll
        _       <- ItemsRepo.insertMany(bulkInsertData)
        uuid     = UUID.fromString(UUID1)
        itemOpt <- ItemsRepo.findFirst(Filter.eq("uuid", UUID1))
        query   <- ItemsRepo.translate(Filter.eq("uuid", UUID1))
      } yield assert(itemOpt.map(_.uuid))(isSome(equalTo(uuid)))
    },
    test("findFirst works") {
      for {
        _       <- ItemsRepo.removeAll
        _       <- ItemsRepo.insertMany(bulkInsertData)
        luisOpt <- ItemsRepo.findFirst(byName("Daniel"))
      } yield assert(luisOpt.map(_.name))(isSome(equalTo("Daniel")))
    },
    test("insert works") {
      for {
        _     <- ItemsRepo.removeAll
        id    <- MongoId.zmake("607ebd5d1c8f40252380ea44")
        item   = Item(Some(id), UUID.randomUUID, "Lorelai")
        _     <- ItemsRepo.insert(item)
        count <- ItemsRepo.count
        found <- ItemsRepo.findFirst(Filter.idEq(id))
      } yield assert(count)(equalTo(1L)) &&
        assert(found.map(_.name))(isSome(equalTo("Lorelai")))
    },
    test("remove works") {
      for {
        _     <- ItemsRepo.removeAll
        _     <- ItemsRepo.insertMany(bulkInsertData)
        _     <- ItemsRepo.remove(inName("Daniel", "John"))
        count <- ItemsRepo.count
      } yield assert(count)(equalTo(1L))
    },
    test("update works") {
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
    test("update document works") {
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
    Filter.eq("name", name)

  def inName(names: String*) =
    Filter.in("name", names)

  val UUID1 = "c6ac38e9-1417-49bd-ab5b-a6081eb40d71"

  def bulkInsertData = Chunk(
    Item(
      Some(MongoId.make),
      UUID.fromString(UUID1),
      "Daniel"
    ),
    Item(Some(MongoId.make), UUID.randomUUID, "Jane"),
    Item(Some(MongoId.make), UUID.randomUUID, "John")
  )
