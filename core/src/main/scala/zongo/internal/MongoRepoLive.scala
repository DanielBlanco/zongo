package zongo.internal

import com.mongodb.client.result._
import org.bson.conversions.Bson
import mongo4cats.bson._
import mongo4cats.client._
import mongo4cats.database._
import mongo4cats.codecs._
import mongo4cats.collection.operations._
import scala.reflect.ClassTag
import zio._
import zio.stream._
import zongo.{Mongo, MongoDoc, MongoRepo, FindQueryBuilder}

/** Very opinionated helper class to facilitate the creation of Mongo
  *  repositories.
  */
abstract class MongoRepoLive[D <: MongoDoc: ClassTag](
    mongo: Mongo.Service
)(implicit cp: MongoCodecProvider[D])
    extends MongoRepo.Service[D] {

  /** The mongo database. */
  val databaseName: String

  /** The mongo collection. */
  val collectionName: String

  /** @see MongoRepo.clearCollection */
  def clearCollection: Task[DeleteResult] =
    _coll_ >>= (c => mongo.clearCollection(c))

  /** @see MongoRepo.count */
  def count: Task[Long] =
    _coll_ >>= (_.count)

  /** @see MongoRepo.count */
  def count(filter: Filter): Task[Long] =
    _coll_ >>= (_.count(filter))

  /** @see MongoRepo.find */
  def find: Task[FindQueryBuilder[D]] =
    _coll_.map(_.find)

  /** @see MongoRepo.insert */
  def insert(doc: D): Task[InsertOneResult] =
    _coll_ >>= (_.insertOne(doc))

  /** @see MongoRepo.insertMany */
  def insertMany(docs: Chunk[D]): Task[InsertManyResult] =
    _coll_ >>= (_.insertMany(docs.toSeq))

  /** @see MongoRepo.remove */
  def remove(filter: Filter): Task[DeleteResult] =
    _coll_ >>= (_.deleteMany(filter))

  /** @see MongoRepo.update */
  def update(
      query: Filter,
      update: Update
  ): Task[UpdateResult] =
    _coll_ >>= (_.updateMany(query, update))

  private def _db_ =
    mongo.getDatabase(databaseName)

  private def _coll_ =
    _db_ >>= (_.getCollectionWithCodec[D](collectionName))

}
