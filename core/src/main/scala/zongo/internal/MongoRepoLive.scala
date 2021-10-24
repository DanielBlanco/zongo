// package zongo.internal

// import mongo4cats.bson._
// import mongo4cats.client._
// import mongo4cats.database._
// // import org.bson.codecs.configuration.{CodecRegistry, CodecRegistries, CodecProvider}
// // import org.mongodb.scala.{Document => _, _}
// // import org.mongodb.scala.bson._
// // import org.mongodb.scala.bson.codecs.Macros
// // import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
// // import org.mongodb.scala.model.{IndexModel, IndexOptions, Updates}
// // import org.mongodb.scala.result._
// import scala.reflect.ClassTag
// import zio._
// import zio.stream._
// import zongo.{Mongo, MongoDoc, MongoRepo}

// /** Very opinionated helper class to facilitate the creation of Mongo
//   *  repositories.
//   */
// abstract class MongoRepoLive[D <: MongoDoc](mongo: Mongo.Service)
//     extends MongoRepo.Service[D] {

//   /** The mongo database. */
//   val databaseName: String

//   /** The mongo collection. */
//   val collectionName: String

//   /** Used by some mongo functions. */
//   implicit val ct: ClassTag[D]

//   /** @see MongoRepo.clearCollection */
//   def clearCollection: Task[Unit] =
//     mongo.clearCollection(_coll)

//   /** @see MongoRepo.count */
//   def count: Task[Long] =
//     mongo.count(_coll)

//   /** @see MongoRepo.count */
//   def count(query: conversions.Bson): Task[Long] =
//     mongo.count(_coll, query)

//   /** @see MongoRepo.find */
//   def find(
//       query: conversions.Bson,
//       sorts: Option[conversions.Bson] = None,
//       limit: Option[Int] = None,
//       skip: Option[Int] = None,
//       projection: Option[conversions.Bson] = None
//   ): Stream[MongoException, D] =
//     mongo
//       .find[Document](_coll, query, sorts, limit, skip, projection)
//       .mapM(zbsonToDoc)

//   /** @see MongoRepo.insert */
//   def insert(doc: D): Task[InsertOneResult] =
//     zdocToBson(doc) >>= { bson =>
//       mongo.insert(_coll, bson)
//     }

//   /** @see MongoRepo.insertMany */
//   def insertMany(docs: Seq[D]): Task[InsertManyResult] =
//     ZIO.foreachPar(docs)(zdocToBson) >>= { bson =>
//       mongo.insertMany(_coll, bson)
//     }

//   /** @see MongoRepo.remove */
//   def remove(query: conversions.Bson): IO[MongoException, Unit] =
//     mongo.remove(_coll, query)

//   /** @see MongoRepo.update */
//   def update(
//       query: conversions.Bson,
//       update: conversions.Bson
//   ): Task[UpdateResult] =
//     mongo.update(_coll, query, update)

//   private def zdocToBson(d: D): IO[MongoException, Document] =
//     ZIO.fromEither(docToBson(d)).mapError { e =>
//       new MongoException(e.getMessage())
//     }

//   private def zbsonToDoc(d: Document): IO[MongoException, D] =
//     ZIO.fromEither(bsonToDoc(d)).mapError { e =>
//       new MongoException(e.getMessage())
//     }

//   private def _db =
//     mongo._database(databaseName)

//   private def _coll =
//     mongo._collection[Document](collectionName)(_db)
// }
