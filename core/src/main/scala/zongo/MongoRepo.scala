package zongo

import com.mongodb.MongoException
import com.mongodb.client.result.*
import org.bson.BsonDocument
import org.bson.conversions.Bson
import mongo4cats.bson.*
import mongo4cats.client.*
import mongo4cats.database.*
import mongo4cats.codecs.*
import mongo4cats.collection.operations.*
import mongo4cats.collection.operations.FilterExt._
import scala.reflect.ClassTag
import zio.*
import zio.stream.*

/** Helper class to facilitate the creation of Mongo repositories. */
case class MongoRepo[D <: MongoDoc: ClassTag](
    mongo: Mongo.Service,
    databaseName: String,
    collectionName: String
)(implicit cp: MongoCodecProvider[D]) {

  /** @see MongoRepo.clearCollection */
  def clearCollection: Task[DeleteResult] =
    getCollection >>= (c => mongo.clearCollection(c))

  /** @see MongoRepo.count */
  def count: Task[Long] =
    getCollection >>= (_.count)

  /** @see MongoRepo.count */
  def count(filter: Filter): Task[Long] =
    getCollection >>= (_.count(filter))

  /** Explain the execution plan for this operation with the server's default
    * verbosity level.
    */
  def explain(filter: Filter): Task[Document] =
    finder.flatMap(_.filter(filter).explain)

  /** Convert a filter into a string which can then be printed. */
  def translate(filter: Filter): Task[String] =
    Task(filter.translate)

  /** @see MongoRepo.finder */
  def finder: Task[FindQueryBuilder[D]] =
    getCollection.map(_.find)

  /** Returns a query builder to find documents matching some criteria. */
  def findChunks: Task[Chunk[D]] =
    finder.flatMap(_.chunks)

  /** Returns a query builder to find documents matching some criteria. */
  def findChunks(filter: Filter): Task[Chunk[D]] =
    finder.flatMap(_.filter(filter).chunks)

  /** Returns a query builder to find documents matching some criteria. */
  def findFirst: Task[Option[D]] =
    finder.flatMap(_.first)

  /** Returns a query builder to find documents matching some criteria. */
  def findFirst(filter: Filter): Task[Option[D]] =
    finder.flatMap(_.filter(filter).first)

  /** @see MongoRepo.insert */
  def insert(doc: D): Task[InsertOneResult] =
    getCollection >>= (_.insertOne(doc))

  /** @see MongoRepo.insertMany */
  def insertMany(docs: Chunk[D]): Task[InsertManyResult] =
    getCollection >>= (_.insertMany(docs.toSeq))

  /** @see MongoRepo.remove */
  def remove(id: MongoId): Task[DeleteResult] =
    remove(Filter.idEq(id))

  /** @see MongoRepo.remove */
  def remove(filter: Filter): Task[DeleteResult] =
    getCollection >>= (_.deleteMany(filter))

  /** @see MongoRepo.update */
  def update(doc: D): Task[UpdateResult] =
    for {
      c      <- getCollection
      filter  = (id: ObjectId) => Document("_id" -> id)
      update  = Document("$set", doc)
      result <- doc._id match {
                  case None     => idNotFound
                  case Some(id) => c.updateOne(filter(id), update)
                }
    } yield result

  /** @see MongoRepo.update */
  def update(
      query: Filter,
      update: Update
  ): Task[UpdateResult] =
    getCollection >>= (_.updateMany(query, update))

  def getDatabase =
    mongo.getDatabase(databaseName)

  def getCollection        =
    getDatabase >>= (_.getCollectionWithCodec[D](collectionName))

  protected def idNotFound =
    Task.fail(new MongoException("Document does not have an id"))

}
