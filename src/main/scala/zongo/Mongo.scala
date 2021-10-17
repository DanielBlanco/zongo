package zongo

import org.mongodb.scala._
import org.mongodb.scala.bson._
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.model.{Filters, IndexModel, Sorts}
import org.mongodb.scala.result._
import scala.util.{Failure, Success, Try}
import scala.reflect.ClassTag
import zio._
import zio.prelude._
import zio.interop.reactivestreams._
import zio.macros.accessible
import zio.stream._
import zongo.internal.MongoLive

@accessible
object Mongo {

  trait Service {

    /** Gets the database with the given name.
      *
      * @param name the name of the database
      * @return the database
      */
    def _database(name: String): MongoDatabase

    /** Gets the database with the given name.
      *
      * @param name the name of the database
      * @return the database wrapped in a ZIO.
      */
    def database(name: String): UIO[MongoDatabase]

    /** Clears the data from all Mongo collections. */
    def clearDatabase(db: MongoDatabase): Task[Unit]

    /** Find the available collections. */
    def findCollectionNames(db: MongoDatabase): Stream[MongoException, String]

    /** Executes command in the context of the current database.
      *
      * @param command        the command to be run
      * @param db             the database to use.
      * @return a ZIO Stream containing the command result.
      */
    def runCommand[A](
        command: conversions.Bson
    )(db: MongoDatabase)(implicit ct: ClassTag[A]): Stream[MongoException, A]

    /** Executes command in the context of the current database.
      *
      * @param command        the command to be run
      * @param readPreference the ReadPreference to be used when executing
      *                       the command
      * @param db             the database to use.
      * @return a ZIO Stream containing the command result.
      */
    def runCommand[A](
        command: conversions.Bson,
        readPreference: ReadPreference
    )(db: MongoDatabase)(implicit ct: ClassTag[A]): Stream[MongoException, A]

    /** Same as ping.
      *
      * @param db   the database to use.
      * @return nothing useful.
      */
    def healthcheck(db: MongoDatabase): IO[MongoException, Unit]

    /** Runs a query and if no error is returned all is good.
      *
      * @param db   the database to use.
      * @return nothing useful.
      */
    def ping(db: MongoDatabase): IO[MongoException, Unit]

    /** Gets the Mongo collection to use.
      *
      * @param name the name of the mongo collection to fetch.
      * @param db   the database to use.
      * @return a MongoCollection instance.
      */
    def _collection[A](name: String)(db: MongoDatabase)(implicit
        ct: ClassTag[A]
    ): MongoCollection[A]

    /** Gets the Mongo collection to use.
      *
      * @param name the name of the mongo collection to fetch.
      * @param db   the database to use.
      * @return a MongoCollection wrapped in a ZIO.
      */
    def collection[A](name: String)(db: MongoDatabase)(implicit
        ct: ClassTag[A]
    ): UIO[MongoCollection[A]]

    /** Clears the data from a Mongo collection.
      *
      * @param c the name of the mongo collection.
      * @return a Task of Unit.
      */
    def clearCollection[A](c: MongoCollection[A]): Task[Unit]

    /** Count all the documents in the collection.
      *
      * @param c the mongo collection.
      * @return a Task containing the number (Long) of documents found.
      */
    def count[A](c: MongoCollection[A]): Task[Long]

    /** Count all the documents in the collection.
      *
      * @param c the mongo collection.
      * @param query the bson document to filter by.
      * @return a Task containing the number (Long) of documents found.
      */
    def count[A](c: MongoCollection[A], query: conversions.Bson): Task[Long]

    /** Create an index in the collection.
      *
      * @param c the mongo collection.
      * @param i the bson representation for the index
      * @return a Task containing the name of the index created.
      */
    def createIndex[A](c: MongoCollection[A], i: conversions.Bson): Task[String]

    /** Create multiple indexes in the collection.
      *
      * @param c the mongo collection.
      * @param l the list of indexes.
      * @return a stream for the names of the indexes created.
      */
    def createIndexes[A](
        c: MongoCollection[A],
        l: Seq[IndexModel]
    ): Stream[MongoException, String]

    /** List all existing indexes in the collection.
      *
      * @param c the mongo collection.
      * @return a Task containing a list with all the indexes in a BsonDocument
      *         representation.
      */
    def listIndexes[A](c: MongoCollection[A]): Stream[MongoException, Document]

    /** Find the documents matching the given criteria.
      *
      * @param c the mongo collection.
      * @param query the bson document to filter by.
      * @param sorts the bson document to sort by (optional).
      * @param limit maximum number of records to return (optional).
      * @param skip number of records to skip (optional).
      * @param projection to select only the necessary data (optional).
      * @return a Task containing a Seq of BsonValue.
      */
    def find[A](
        c: MongoCollection[A],
        query: conversions.Bson,
        sorts: Option[conversions.Bson] = None,
        limit: Option[Int] = None,
        skip: Option[Int] = None,
        projection: Option[conversions.Bson] = None
    )(implicit ct: ClassTag[A]): Stream[MongoException, A]

    /** Find documents using distinct. */
    def distinct[A](
        c: MongoCollection[A],
        field: String,
        query: conversions.Bson
    ): Stream[MongoException, BsonValue]

    /** Inserts the provided document.
      *
      * @param c the mongo collection.
      * @param doc the document to insert.
      * @return
      */
    def insert[A](c: MongoCollection[A], doc: A): Task[InsertOneResult]

    /** Inserts a batch of documents. The preferred way to perform bulk inserts
      *  is to use the BulkWrite API.
      *
      *  However, when talking with a server &lt; 2.6, using this method will be
      *  faster due to constraints in the bulk API related to error handling.
      *
      * @param docs the documents to insert
      * @return a Task[InsertManyResult]
      */
    def insertMany[A](c: MongoCollection[A], docs: Seq[A]): Task[InsertManyResult]

    /** Deletes the documents that match the provided query.
      *
      * @param c the mongo collection.
      * @param query to filter by.
      * @return nothing useful.
      */
    def remove[A](
        c: MongoCollection[A],
        query: conversions.Bson
    ): IO[MongoException, Unit]

    /** Updates records in database.
      *
      * @example
      * {{{
      *   Mongo.update(
      *     "users",
      *     Filters.eq("name", "Liz"),
      *     Updates.set("name", "Lizzy")
      *   )
      * }}}
      *
      * @param c the mongo collection.
      * @param query How to find the records to update (selector).
      * @param update The bson values to update (update).
      * @return the update result
      */
    def update[A](
        c: MongoCollection[A],
        query: conversions.Bson,
        update: conversions.Bson
    ): Task[UpdateResult]
  }

  /** Constructs a layer from a MongoClient.
    *
    * @param uri to connect.
    * @return a ZLayer.
    */
  def live(uri: String): ULayer[Mongo] =
    MongoLive(uri).toLayer.orDie

  /** @note This will help us debug queries. */
  lazy val toBsonDocument: conversions.Bson => BsonDocument =
    _.toBsonDocument(BsonDocument.getClass, DEFAULT_CODEC_REGISTRY)
}
