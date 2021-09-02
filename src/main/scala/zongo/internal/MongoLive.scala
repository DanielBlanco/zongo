package zongo.internal

import java.io.Closeable
import org.mongodb.scala.{Document => _, _}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson._
import org.mongodb.scala.model.{IndexModel, Sorts}
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
    findCollectionNames(db).runCollect.map(_ => ())

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
}
object MongoLive {
  def apply(uri: String): Managed[Throwable, Mongo.Service] =
    Managed.make(connect(uri))(close)

  private def connect(uri: String) =
    Task.effect(new MongoLive(MongoClient(uri)))

  private def close(c: Closeable) =
    ZIO.effectTotal(c.close)
}
