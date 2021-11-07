package zongo.support

import com.mongodb.MongoException
import com.mongodb.client.result.*
import java.time.{Instant, LocalDate}
import mongo4cats.collection.operations.*
import zio.*
import zio.json.*
import zio.macros.*
import zio.stream.*
import zongo.*
import zongo.json.*

case class Item(
    _id: Option[MongoId],
    name: String,
    createdAt: Instant = Instant.now(),
    updatedAt: LocalDate = LocalDate.now()
) extends MongoDoc
object Item {
  implicit val jsonDecoder: JsonDecoder[Item] = DeriveJsonDecoder.gen[Item]
  implicit val jsonEncoder: JsonEncoder[Item] = DeriveJsonEncoder.gen[Item]
}

@accessible
object ItemsRepo {
  trait Service {

    def clearCollection: Task[DeleteResult]

    def count: Task[Long]

    def count(filter: Filter): Task[Long]

    def finder: Task[FindQueryBuilder[Item]]

    def findChunks: Task[Chunk[Item]]

    def findChunks(filter: Filter): Task[Chunk[Item]]

    def findFirst: Task[Option[Item]]

    def findFirst(filter: Filter): Task[Option[Item]]

    def insert(doc: Item): Task[Item]

    def insertMany(docs: Chunk[Item]): Task[InsertManyResult]

    def remove(id: MongoId): Task[DeleteResult]

    def remove(filter: Filter): Task[DeleteResult]

    def removeAll: Task[DeleteResult] = clearCollection

    def update(doc: Item): Task[UpdateResult]

    def update(query: Filter, update: Update): Task[UpdateResult]
  }

  case class Live(repo: MongoRepo[Item]) extends Service {

    def clearCollection: Task[DeleteResult] = repo.clearCollection

    def count: Task[Long] = repo.count

    def count(filter: Filter): Task[Long] = repo.count(filter)

    def finder: Task[FindQueryBuilder[Item]] = repo.finder

    def findChunks: Task[Chunk[Item]] = repo.findChunks

    def findChunks(filter: Filter): Task[Chunk[Item]] = repo.findChunks(filter)

    def findFirst: Task[Option[Item]] = repo.findFirst

    def findFirst(filter: Filter): Task[Option[Item]] = repo.findFirst(filter)

    def insert(doc: Item): Task[Item] =
      for {
        _       <- repo.insert(doc)
        opt     <- doc._id match {
                     case None      => ZIO.none
                     case Some(_id) => repo.findFirst(Filter.idEq(_id))
                   }
        updated <- ZIO
                     .fromOption(opt)
                     .mapError(_ => new MongoException("Inserted item not found"))
      } yield updated

    def insertMany(docs: Chunk[Item]): Task[InsertManyResult] =
      repo.insertMany(docs)

    def remove(id: MongoId): Task[DeleteResult] =
      repo.remove(id)

    def remove(filter: Filter): Task[DeleteResult] =
      repo.remove(filter)

    def update(doc: Item): Task[UpdateResult] =
      repo.update(doc)

    def update(query: Filter, update: Update): Task[UpdateResult] =
      repo.update(query, update)
  }

  def layer(db: String): URLayer[Mongo, ItemsRepo] =
    ZLayer.fromService[Mongo.Service, Service] { mongo =>
      Live(MongoRepo[Item](mongo, db, "items"))
    }
}
