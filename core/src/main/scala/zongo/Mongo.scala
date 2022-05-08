package zongo

import com.mongodb.ReadPreference
import com.mongodb.client.result.DeleteResult
import org.bson.{BsonDocument, UuidRepresentation}
import org.bson.codecs.*
import org.bson.codecs.configuration.CodecRegistries
import org.bson.conversions.Bson
import mongo4cats.bson.*
import mongo4cats.codecs.CodecRegistry
import mongo4cats.client.{MongoClient => CatsMongoClient}
import mongo4cats.collection.{MongoCollection => CatsMongoCollection}
import mongo4cats.database.{MongoDatabase => CatsMongoDatabase}
import mongo4cats.collection.queries.{FindQueryBuilder => CatsFindQueryBuilder}
import mongo4cats.collection.operations.{Filter, Sort => CatsSort}
import scala.util.{Failure, Success, Try}
import scala.reflect.ClassTag
import zio.*
import zio.prelude.*
import zio.interop.reactivestreams.*
import zio.interop.catz.*
import zio.stream.*
import zio.stream.interop.fs2z.*
import zongo.internal.MongoLive

type MongoId = MongoId.Type

type MongoUri = MongoUri.Type

type MongoDatabase = CatsMongoDatabase[Task]

type MongoClient = CatsMongoClient[Task]

type MongoCollection[A] = CatsMongoCollection[Task, A]

type FindQueryBuilder[A] = CatsFindQueryBuilder[Task, A]

type Sort = CatsSort

extension [A](a: FindQueryBuilder[A])
  def chunks: Task[Chunk[A]] =
    a.all.map(Chunk.fromIterable)

  def zstream[R](queueSize: Int = 16): ZStream[R, Throwable, A] =
    a.stream.toZStream(queueSize)

trait Mongo:

  /** Gets the database with the given name.
    *
    * @param name
    *   the name of the database
    * @return
    *   the database wrapped in a ZIO.
    */
  def getDatabase(name: String): Task[MongoDatabase]

  /** Drops the database.
    *
    * @param db
    *   the database to drop.
    * @return
    *   unit.
    */
  def dropDatabase(db: MongoDatabase): Task[Unit]

  /** Clears the data from all Mongo collections. */
  def clearDatabase(db: MongoDatabase): Task[Unit] =
    for {
      names <- findCollectionNames(db)
      colls <- getCollections(names)(db)
      _     <- clearCollections(colls)
    } yield ()

  /** Find the available collections. */
  def findCollectionNames(db: MongoDatabase): Task[Chunk[String]]

  /** Executes command in the context of the current database.
    *
    * @param command
    *   the command to be run
    * @param db
    *   the database to use.
    * @return
    *   a ZIO Stream containing the command result.
    */
  def runCommand(command: Bson)(db: MongoDatabase): Task[Document]

  /** Executes command in the context of the current database.
    *
    * @param command
    *   the command to be run
    * @param readPreference
    *   the ReadPreference to be used when executing the command
    * @param db
    *   the database to use.
    * @return
    *   a ZIO Stream containing the command result.
    */
  def runCommand(
      command: Bson,
      readPreference: ReadPreference
  )(db: MongoDatabase): Task[Document]

  /** Same as ping.
    *
    * @param db
    *   the database to use.
    * @return
    *   nothing useful.
    */
  def healthcheck(db: MongoDatabase): Task[Unit] =
    ping(db).map(_ => ())

  /** Runs a query and if no error is returned all is good.
    *
    * @param db
    *   the database to use.
    * @return
    *   pong
    */
  def ping(db: MongoDatabase): Task[String] =
    runCommand(Document("ping" -> "ping"))(db).map(_ => "pong")

  /** Create a mongo collection.
    *
    * @param name
    *   the name of the mongo collection to create.
    * @param db
    *   the database to use.
    * @return
    *   unit.
    */
  def createCollection(name: String)(
      db: MongoDatabase
  ): Task[Unit] =
    runCommand(Document("create" -> name))(db).map(_ => ())

  /** Create a chunk of mongo collections.
    *
    * @param name
    *   the names of the mongo collections to create.
    * @param db
    *   the database to use.
    * @return
    *   unit.
    */
  def createCollections(names: Chunk[String])(
      db: MongoDatabase
  ): Task[Unit] =
    ZIO.foreachPar(names)(createCollection(_)(db)).map(_ => ())

  /** Removes ALL records from a mongo collection.
    *
    * @param name
    *   the names of the mongo collections to create.
    * @param db
    *   the database to use.
    * @return
    *   unit.
    */
  def clearCollection[A](c: MongoCollection[A]): Task[DeleteResult]

  /** Removes ALL records from chunk of mongo collection.
    *
    * @param name
    *   the names of the mongo collections to create.
    * @param db
    *   the database to use.
    * @return
    *   unit.
    */
  def clearCollections[A](
      cs: Chunk[MongoCollection[A]]
  ): Task[Chunk[DeleteResult]] =
    ZIO.foreachPar(cs)(clearCollection)

  /** Gets the Mongo collection to use.
    *
    * @param name
    *   the name of the mongo collection to fetch.
    * @param db
    *   the database to use.
    * @return
    *   a MongoCollection instance.
    */
  def getCollection(
      name: String
  )(db: MongoDatabase): Task[MongoCollection[Document]]

  /** Gets the Mongo collection to use for some Type.
    *
    * @param name
    *   the name of the mongo collection to fetch.
    * @param db
    *   the database to use.
    * @return
    *   a MongoCollection instance.
    */
  def getCollection[A: ClassTag](
      name: String,
      codecRegistry: CodecRegistry
  )(db: MongoDatabase): Task[MongoCollection[A]]

  /** Gets the Mongo collections.
    *
    * @param name
    *   the chunk of names to fetch.
    * @param db
    *   the database to use.
    * @return
    *   a MongoCollection instance.
    */
  def getCollections(
      names: Chunk[String]
  )(db: MongoDatabase): Task[Chunk[MongoCollection[Document]]] =
    ZIO.foreachPar(names)(getCollection(_)(db))

  /** Drops a mongo collection from database.
    *
    * @param c
    *   the mongo collection to drop.
    * @param db
    *   the database to use.
    * @return
    *   unit.
    */
  def dropCollection[A](c: MongoCollection[A]): Task[Unit]

  /** Drops a chunk of mongo collections from database.
    *
    * @param cs
    *   the chunk of collections to drop.
    * @param db
    *   the database to use.
    * @return
    *   unit.
    */
  def dropCollections[A](cs: Chunk[MongoCollection[A]]): Task[Unit] =
    ZIO.foreachPar(cs)(dropCollection(_)).map(_ => ())

object Mongo:
  type MongoIO[A] = RIO[Mongo, A]

  def getDatabase(name: String): MongoIO[MongoDatabase] =
    ZIO.serviceWithZIO(_.getDatabase(name))

  def dropDatabase(db: MongoDatabase): MongoIO[Unit] =
    ZIO.serviceWithZIO(_.dropDatabase(db))

  def clearDatabase(db: MongoDatabase): MongoIO[Unit] =
    ZIO.serviceWithZIO(_.clearDatabase(db))

  def findCollectionNames(db: MongoDatabase): MongoIO[Chunk[String]] =
    ZIO.serviceWithZIO(_.findCollectionNames(db))

  def runCommand(command: Bson)(db: MongoDatabase): MongoIO[Document] =
    ZIO.serviceWithZIO(_.runCommand(command)(db))

  def runCommand(
      command: Bson,
      readPreference: ReadPreference
  )(db: MongoDatabase): MongoIO[Document] =
    ZIO.serviceWithZIO(_.runCommand(command, readPreference)(db))

  def healthcheck(db: MongoDatabase): MongoIO[Unit] =
    ZIO.serviceWithZIO(_.healthcheck(db))

  def ping(db: MongoDatabase): MongoIO[String] =
    ZIO.serviceWithZIO(_.ping(db))

  def createCollection(name: String)(
      db: MongoDatabase
  ): MongoIO[Unit] =
    ZIO.serviceWithZIO(_.createCollection(name)(db))

  def createCollections(names: Chunk[String])(
      db: MongoDatabase
  ): MongoIO[Unit] =
    ZIO.serviceWithZIO(_.createCollections(names)(db))

  def clearCollection[A](c: MongoCollection[A]): MongoIO[DeleteResult] =
    ZIO.serviceWithZIO(_.clearCollection(c))

  def clearCollections[A](
      cs: Chunk[MongoCollection[A]]
  ): MongoIO[Chunk[DeleteResult]] =
    ZIO.serviceWithZIO(_.clearCollections(cs))

  def getCollection(
      name: String
  )(db: MongoDatabase): MongoIO[MongoCollection[Document]] =
    ZIO.serviceWithZIO(_.getCollection(name)(db))

  def getCollection[A: ClassTag](
      name: String,
      codecRegistry: CodecRegistry
  )(db: MongoDatabase): MongoIO[MongoCollection[A]] =
    ZIO.serviceWithZIO(_.getCollection(name, codecRegistry)(db))

  def getCollections(
      names: Chunk[String]
  )(db: MongoDatabase): MongoIO[Chunk[MongoCollection[Document]]] =
    ZIO.serviceWithZIO(_.getCollections(names)(db))

  def dropCollection[A](c: MongoCollection[A]): MongoIO[Unit] =
    ZIO.serviceWithZIO(_.dropCollection(c))

  def dropCollections[A](cs: Chunk[MongoCollection[A]]): MongoIO[Unit] =
    ZIO.serviceWithZIO(_.dropCollections(cs))

  /** Constructs a layer from a MongoClient.
    *
    * @param uri
    *   to connect.
    * @return
    *   a ZLayer.
    */
  def live(implicit tag: Tag[MongoUri]): URLayer[MongoUri, Mongo] =
    ZLayer.fromFunctionManaged(env => MongoLive(env.get).orDie)

  /** Constructs a layer from a MongoClient.
    *
    * @param uri
    *   to connect.
    * @return
    *   a ZLayer.
    */
  def live(uri: MongoUri): ULayer[Mongo] =
    MongoLive(uri).toLayer[Mongo].orDie

  /** @note This will help us debug queries. */
  lazy val bsonToBsonDocument: Bson => BsonDocument =
    _.toBsonDocument(
      classOf[BsonDocument],
      CodecRegistries.fromRegistries(
        CodecRegistries.fromCodecs(new UuidCodec(UuidRepresentation.STANDARD)),
        Bson.DEFAULT_CODEC_REGISTRY
      )
    )
