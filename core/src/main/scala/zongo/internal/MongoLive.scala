package zongo.internal

import com.mongodb.ReadPreference
import java.io.Closeable
import org.bson.conversions.Bson
import mongo4cats.bson._
import mongo4cats.codecs.CodecRegistry
// import mongo4cats.client._
// import mongo4cats.database._
// import org.mongodb.scala.{Document => _, _}
// import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
// import org.mongodb.scala.bson._
// import org.mongodb.scala.model.{IndexModel, Sorts}
// import org.mongodb.scala.result._
import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag
import zio._
import zio.prelude._
import zio.stream._
import zio.interop.catz._
import zio.interop.catz.implicits._
import zio.interop.reactivestreams._
import zio.stream.interop.fs2z._
import zongo.{Mongo, MongoClient, MongoDatabase, MongoCollection}

final case class MongoLive(
    val client: MongoClient
) extends Mongo.Service {

  /** Closes the MongoClient resource. */
  // override def close: Unit =
  //   client.close()

  /** @see Mongo.Service._database */
  // def _database(name: String): MongoDatabase =
  //   client.getDatabase(name)

  /** @see Mongo.Service.database */
  def getDatabase(name: String): Task[MongoDatabase] =
    client.getDatabase(name)

  /** @see Mongo.Service.clearDatabase */
  // def clearDatabase(db: MongoDatabase): Task[Unit] =
  //   for {
  //     names <- findCollectionNames(db).runCollect
  //     _     <- ZIO.foreach(names) { name =>
  //                collection(name)(db).flatMap(clearCollection[BsonValue])
  //              }
  //   } yield ()

  /** @see Mongo.Service.runCommand */
  def runCommand(
      command: Bson
  )(db: MongoDatabase): Task[Document] =
    db.runCommand(command)

  // /** @see Mongo.Service.runCommand */
  def runCommand(
      command: Bson,
      readPreference: ReadPreference
  )(db: MongoDatabase): Task[Document] =
    db.runCommand(command, readPreference)

  /** @see Mongo.Service.findCollectionNames */
  def findCollectionNames(db: MongoDatabase): Task[Chunk[String]] =
    db.listCollectionNames.map(_.toChunk)

  // /** @see Mongo.Service.clearCollection */
  // def clearCollection[A](c: MongoCollection[A]): Task[Unit] =
  //   remove(c, Document())

  /** @see Mongo.Service.getCollection */
  def getCollection(
      name: String
  )(db: MongoDatabase): Task[MongoCollection[Document]] =
    db.getCollection(name)

  /** @see Mongo.Service.getCollection */
  def getCollection[A: ClassTag](
      name: String,
      codecRegistry: CodecRegistry
  )(db: MongoDatabase): Task[MongoCollection[A]] =
    db.getCollection(name, codecRegistry)

}
object MongoLive {

  def apply(uri: String): Managed[Throwable, Mongo.Service] =
    connect(uri).map(client => new MongoLive(client))

  private def connect(uri: String) =
    mongo4cats.client.MongoClient.fromConnectionString[Task](uri).toManagedZIO
}
