import mongo4cats.bson._
import mongo4cats.client.{MongoClient => CatsMongoClient}
import mongo4cats.collection.{MongoCollection => CatsMongoCollection}
import mongo4cats.database.{MongoDatabase => CatsMongoDatabase}
import mongo4cats.collection.queries.{FindQueryBuilder => CatsFindQueryBuilder}
import zio._
import zio.prelude._
import zongo.internal._

package object zongo extends MongoIdType with MongoUriType {
  type Mongo = Has[Mongo.Service]

  type MongoDatabase = CatsMongoDatabase[Task]

  type MongoClient = CatsMongoClient[Task]

  type MongoCollection[A] = CatsMongoCollection[Task, A]

  type FindQueryBuilder[A] = CatsFindQueryBuilder[Task, A]
}
