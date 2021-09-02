package zongo

import org.mongodb.scala._
import org.mongodb.scala.bson._
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.model.{Filters, IndexModel, Sorts}
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
    def database(name: String): Task[MongoDatabase]

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
      * @param readPreference the [[ReadPreference]] to be used when executing
      *                       the command
      * @param db             the database to use.
      * @return a ZIO Stream containing the command result.
      */
    def runCommand[A](
        command: conversions.Bson,
        readPreference: ReadPreference
    )(db: MongoDatabase)(implicit ct: ClassTag[A]): Stream[MongoException, A]

    /** Runs a query and if no error is returned all is good.
      *
      * @param db   the database to use.
      * @return nothing useful.
      */
    def healthcheck(db: MongoDatabase): IO[MongoException, Unit]

    /** Gets the Mongo collection to use.
      *
      * @param name the name of the mongo collection to fetch.
      * @param db   the database to use.
      * @return a MongoCollection instance.
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

    def remove[A](
        c: MongoCollection[A],
        query: conversions.Bson
    ): IO[MongoException, Unit]
  }

  /** Constructs a layer from a MongoClient.
    *
    * @param uri to connect.
    * @return a ZLayer.
    */
  def live(uri: String): ULayer[Mongo] =
    MongoLive(uri).toLayer.orDie
}
