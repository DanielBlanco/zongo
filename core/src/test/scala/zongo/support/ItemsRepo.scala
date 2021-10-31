package zongo.support

import com.mongodb.client.result._
import java.time.{Instant, LocalDate}
import mongo4cats.collection.operations._
import zio._
import zio.json._
import zio.macros._
import zio.stream._
import zongo._
import zongo.json._
import zongo.internal.MongoRepoLive

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
  trait Service extends MongoRepo.Service[Item] {

    override val collectionName = "items"

    /** definition to get @accessible functions */
    def clearCollection: Task[DeleteResult]

    /** definition to get @accessible functions */
    def count: Task[Long]

    /** definition to get @accessible functions */
    def count(filter: Filter): Task[Long]

    /** definition to get @accessible functions */
    def find: Task[FindQueryBuilder[Item]]

    /** definition to get @accessible functions */
    def insert(doc: Item): Task[InsertOneResult]

    /** definition to get @accessible functions */
    def insertMany(docs: Chunk[Item]): Task[InsertManyResult]

    /** definition to get @accessible functions */
    def remove(filter: Filter): Task[DeleteResult]

    /** definition to get @accessible functions */
    def removeAll: Task[DeleteResult]

    /** definition to get @accessible functions */
    def update(query: Filter, update: Update): Task[UpdateResult]
  }

  case class Live(
      mongo: Mongo.Service,
      databaseName: String
  ) extends MongoRepoLive[Item](mongo)
      with Service

  def layer(db: String): URLayer[Mongo, ItemsRepo] =
    ZLayer.fromService[Mongo.Service, Service](Live(_, db))
}
