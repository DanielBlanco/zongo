import mongo4cats.bson._
import mongo4cats.client.{MongoClient => CatsMongoClient}
import mongo4cats.collection.{MongoCollection => CatsMongoCollection}
import mongo4cats.database.{MongoDatabase => CatsMongoDatabase}
import mongo4cats.collection.queries.{FindQueryBuilder => CatsFindQueryBuilder}
import zio._
import zio.stream._
import zio.stream.interop.fs2z._
import zio.interop.catz._
import zio.prelude._
import zio.test.Assertion.{hasSizeString, isLessThan}

package object zongo {
  type Mongo = Has[Mongo.Service]

  type MongoId = MongoId.Type

  type MongoUri = MongoUri.Type

  type MongoDatabase = CatsMongoDatabase[Task]

  type MongoClient = CatsMongoClient[Task]

  type MongoCollection[A] = CatsMongoCollection[Task, A]

  type FindQueryBuilder[A] = CatsFindQueryBuilder[Task, A]

  private[zongo] def nonEmpty = hasSizeString(isGreaterThan(0))

  implicit class FindQueryBuilderOps[A](a: FindQueryBuilder[A]) {

    def chunks: Task[Chunk[A]] =
      a.all.map(Chunk.fromIterable)

    def zstream[R](queueSize: Int = 16): ZStream[R, Throwable, A] =
      a.stream.toZStream(queueSize)
  }
}
