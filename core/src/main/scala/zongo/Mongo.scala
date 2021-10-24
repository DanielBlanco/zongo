package zongo

import com.mongodb.ReadPreference
import org.bson.conversions.Bson
import mongo4cats.bson._
import mongo4cats.codecs.CodecRegistry
// import mongo4cats.client._
// import mongo4cats.database._
// import org.mongodb.scala._
// import org.mongodb.scala.bson._
// import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
// import org.mongodb.scala.model.{Filters, IndexModel, Sorts}
// import org.mongodb.scala.result._
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
      * @return the database wrapped in a ZIO.
      */
    def getDatabase(name: String): Task[MongoDatabase]

    /** Clears the data from all Mongo collections. */
    // def clearDatabase(db: MongoDatabase): Task[Unit]

    /** Find the available collections. */
    def findCollectionNames(db: MongoDatabase): Task[Chunk[String]]

    /** Executes command in the context of the current database.
      *
      * @param command        the command to be run
      * @param db             the database to use.
      * @return a ZIO Stream containing the command result.
      */
    def runCommand(command: Bson)(db: MongoDatabase): Task[Document]

    /** Executes command in the context of the current database.
      *
      * @param command        the command to be run
      * @param readPreference the ReadPreference to be used when executing
      *                       the command
      * @param db             the database to use.
      * @return a ZIO Stream containing the command result.
      */
    def runCommand(
        command: Bson,
        readPreference: ReadPreference
    )(db: MongoDatabase): Task[Document]

    /** Same as ping.
      *
      * @param db   the database to use.
      * @return nothing useful.
      */
    def healthcheck(db: MongoDatabase): Task[Unit] =
      ping(db).map(_ => ())

    /** Runs a query and if no error is returned all is good.
      *
      * @param db the database to use.
      * @return pong
      */
    def ping(db: MongoDatabase): Task[String] =
      runCommand(Document("ping" -> "ping"))(db).map(_ => "pong")

    /** Create a mongo collection.
      *
      * @param name the name of the mongo collection to create.
      * @param db   the database to use.
      * @return unit.
      */
    def createCollection(name: String)(
        db: MongoDatabase
    ): Task[Unit]                             =
      runCommand(Document("create" -> name))(db).map(_ => ())

    /** Create a chunk of mongo collections.
      *
      * @param name the names of the mongo collections to create.
      * @param db   the database to use.
      * @return unit.
      */
    def createCollections(names: Chunk[String])(
        db: MongoDatabase
    ): Task[Unit]                             =
      ZIO.foreachPar(names)(createCollection(_)(db)).map(_ => ())

    /** Gets the Mongo collection to use.
      *
      * @param name the name of the mongo collection to fetch.
      * @param db   the database to use.
      * @return a MongoCollection instance.
      */
    def getCollection(
        name: String
    )(db: MongoDatabase): Task[MongoCollection[Document]]

    /** Gets the Mongo collection to use for some Type.
      *
      * @param name the name of the mongo collection to fetch.
      * @param db   the database to use.
      * @return a MongoCollection instance.
      */
    def getCollection[A: ClassTag](
        name: String,
        codecRegistry: CodecRegistry
    )(db: MongoDatabase): Task[MongoCollection[A]]

    /** Removes a mongo collection from database.
      *
      * @param name the name of the mongo collection to remove.
      * @param db   the database to use.
      * @return unit.
      */
    def removeCollection(name: String)(
        db: MongoDatabase
    ): Task[Unit] =
      runCommand(Document("drop" -> name))(db).map(_ => ())

    /** Removes a chunk of mongo collections from database.
      *
      * @param names the names of the mongo collections to remove.
      * @param db   the database to use.
      * @return unit.
      */
    def removeCollections(names: Chunk[String])(
        db: MongoDatabase
    ): Task[Unit] =
      ZIO
        .foreachPar(names) { name =>
          removeCollection(name)(db)
        }
        .map(_ => ())

  }

  /** Constructs a layer from a MongoClient.
    *
    * @param uri to connect.
    * @return a ZLayer.
    */
  def live(uri: String): ULayer[Mongo] =
    MongoLive(uri).toLayer.orDie

  /** @note This will help us debug queries. */
  // lazy val toBsonDocument: conversions.Bson => BsonDocument =
  //   _.toBsonDocument(BsonDocument.getClass, DEFAULT_CODEC_REGISTRY)
}
