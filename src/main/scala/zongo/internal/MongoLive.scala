package zongo.internal

import java.io.Closeable
import org.mongodb.scala.{Document => _, _}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson._
import org.mongodb.scala.model.{IndexModel, Sorts}
import org.mongodb.scala.result._
import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag
import zio._
import zio.prelude._
import zio.interop.reactivestreams._
import zio.stream._
import zongo.Mongo

final case class MongoLive(
    val client: MongoClient
) extends Mongo.Service
    with Closeable {

  /** Closes the MongoClient resource. */
  override def close: Unit =
    client.close()

  /** @see Mongo.Service.database */
  def database(name: String): Task[MongoDatabase] =
    ZIO.effect(client.getDatabase(name))

  /** @see Mongo.Service.clearDatabase */
  def clearDatabase(db: MongoDatabase): Task[Unit] =
    for {
      names <- findCollectionNames(db).runCollect
      _     <- ZIO.foreach(names) { name =>
                 collection(name)(db).flatMap(clearCollection[BsonValue])
               }
    } yield ()

  /** @see Mongo.Service.runCommand */
  def runCommand[A](
      command: conversions.Bson
  )(db: MongoDatabase)(implicit ct: ClassTag[A]): Stream[MongoException, A] =
    db.runCommand[A](command).toStream().refineToOrDie[MongoException]

  /** @see Mongo.Service.runCommand */
  def runCommand[A](
      command: conversions.Bson,
      readPreference: ReadPreference
  )(db: MongoDatabase)(implicit ct: ClassTag[A]): Stream[MongoException, A] =
    db.runCommand[A](command, readPreference)
      .toStream()
      .refineToOrDie[MongoException]

  /** @see Mongo.Service.healthcheck */
  def healthcheck(db: MongoDatabase): IO[MongoException, Unit] =
    ping(db)

  /** @see Mongo.Service.ping */
  def ping(db: MongoDatabase): IO[MongoException, Unit] =
    runCommand[Document](
      Document("ping" -> "ping")
    )(db).runCollect.map(_ => ())

  /** @see Mongo.Service.findCollectionNames */
  def findCollectionNames(db: MongoDatabase): Stream[MongoException, String] =
    db.listCollectionNames()
      .toStream()
      .refineToOrDie[MongoException]

  /** @see Mongo.Service.clearCollection */
  def clearCollection[A](c: MongoCollection[A]): Task[Unit] =
    remove(c, Document())

  /** @see Mongo.Service.collection */
  def collection[A](name: String)(db: MongoDatabase)(implicit
      ct: ClassTag[A]
  ): UIO[MongoCollection[A]] =
    ZIO.succeed(db.getCollection[A](name))

  /** @see Mongo.Service.count */
  def count[A](c: MongoCollection[A]): Task[Long] =
    count(c, Document())

  /** @see Mongo.Service.count */
  def count[A](c: MongoCollection[A], query: conversions.Bson): Task[Long] =
    c.countDocuments(query)
      .toStream()
      .runCollect
      .map(_.head)
      .refineToOrDie[MongoException]

  /** @see Mongo.Service.createIndex */
  def createIndex[A](c: MongoCollection[A], i: conversions.Bson): Task[String] =
    c.createIndex(i)
      .toStream()
      .runCollect
      .map(_.head)
      .refineToOrDie[MongoException]

  /** @see Mongo.Service.createIndexes */
  def createIndexes[A](
      c: MongoCollection[A],
      l: Seq[IndexModel]
  ): Stream[MongoException, String] =
    c.createIndexes(l).toStream().refineToOrDie[MongoException]

  /** @see Mongo.Service.listIndexes */
  def listIndexes[A](c: MongoCollection[A]): Stream[MongoException, Document] =
    c.listIndexes().toStream().refineToOrDie[MongoException]

  def find[A](
      c: MongoCollection[A],
      query: conversions.Bson,
      sorts: Option[conversions.Bson] = None,
      limit: Option[Int] = None,
      skip: Option[Int] = None,
      projection: Option[conversions.Bson] = None
  ): Stream[MongoException, Document] = {
    val q0 = c.find[Document](query)
    val q1 = projection.fold(q0)(p => q0.projection(p))
    val q2 = sorts.fold(q1)(s => q1.sort(Sorts.orderBy(s)))
    val q3 = limit.fold(q2)(l => q2.limit(l))
    val q4 = skip.fold(q3)(s => q2.skip(s))
    q4.toStream().refineToOrDie[MongoException]
  }

  /** @see Mongo.Service.distinct */
  def distinct[A](
      c: MongoCollection[A],
      field: String,
      query: conversions.Bson
  ): Stream[MongoException, BsonValue] =
    c.distinct[BsonValue](field, query)
      .toStream()
      .refineToOrDie[MongoException]

  /** @see Mongo.Service.insert */
  def insert[A](c: MongoCollection[A], doc: A): Task[InsertOneResult] =
    c.insertOne(doc).toStream().runCollect.map(_.head)

  /** @see Mongo.Service.insertMany */
  def insertMany[A](c: MongoCollection[A], docs: Seq[A]): Task[InsertManyResult] =
    c.insertMany(docs).toStream().runCollect.map(_.head)

  /** @see Mongo.Service.remove */
  def remove[A](
      c: MongoCollection[A],
      query: conversions.Bson
  ): IO[MongoException, Unit] =
    ZIO
      .fromFuture { implicit ec: ExecutionContext =>
        c.deleteMany(query).toFuture()
      }
      .map(_ => ())
      .refineToOrDie[MongoException]

  /** @see Mongo.Service.update */
  def update[A](
      c: MongoCollection[A],
      query: conversions.Bson,
      update: conversions.Bson
  ): Task[UpdateResult] =
    c.updateMany(query, update)
      .toStream()
      .runCollect
      .map(_.head)
      .refineToOrDie[MongoException]
}
object MongoLive {
  def apply(uri: String): Managed[Throwable, Mongo.Service] =
    Managed.make(connect(uri))(close)

  private def connect(uri: String) =
    Task.effect(new MongoLive(MongoClient(uri)))

  private def close(c: Closeable) =
    ZIO.effectTotal(c.close)
}
