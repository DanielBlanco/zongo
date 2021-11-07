import mongo4cats.bson.*
import mongo4cats.client.{MongoClient => CatsMongoClient}
import mongo4cats.collection.{MongoCollection => CatsMongoCollection}
import mongo4cats.database.{MongoDatabase => CatsMongoDatabase}
import mongo4cats.collection.queries.{FindQueryBuilder => CatsFindQueryBuilder}
import zio.*
import zio.stream.*
import zio.stream.interop.fs2z.*
import zio.interop.catz.*
import zio.prelude.*

package object zongo {
  type Mongo = Has[Mongo.Service]

  type MongoId = MongoId.Type

  type MongoUri = MongoUri.Type

  type MongoDatabase = CatsMongoDatabase[Task]

  type MongoClient = CatsMongoClient[Task]

  type MongoCollection[A] = CatsMongoCollection[Task, A]

  type FindQueryBuilder[A] = CatsFindQueryBuilder[Task, A]

  implicit class FindQueryBuilderOps[A](a: FindQueryBuilder[A]) {

    def chunks: Task[Chunk[A]] =
      a.all.map(Chunk.fromIterable)

    def zstream[R](queueSize: Int = 16): ZStream[R, Throwable, A] =
      a.stream.toZStream(queueSize)
  }
}
