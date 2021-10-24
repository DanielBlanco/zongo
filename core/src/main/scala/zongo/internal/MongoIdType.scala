package zongo.internal

// import org.mongodb.scala.bson._
import mongo4cats.bson._
import scala.util.Try
import zio._
import zio.prelude._

trait MongoIdType {

  /** Mongo ID wrapper. */
  object MongoId extends Subtype[ObjectId] {

    def make: MongoId = wrap(newId)

    def make[ID](id: ID): Either[String, MongoId] =
      newId(id).map(wrap)

    def zmake[ID](id: ID): IO[String, MongoId] =
      znewId(id).map(wrap)

    /** This way models can just do <Model>.newId and encapsulate the Id
      *  generation code that is specific to Mongo.
      */
    private def newId = new ObjectId()

    /** Parses an Id or fails with an Exception. */
    private def newId[ID](id: ID): Either[String, ObjectId] =
      id match {
        case _id: String   =>
          Try(new ObjectId(_id)).toEither.fold(
            e => Left(invalidId(_id)),
            s => Right(s)
          )
        case _id: ObjectId => Right(_id)
        case _id           => Left(invalidId(_id))
      }

    private def znewId[ID](id: ID): IO[String, ObjectId] =
      ZIO.fromEither(newId(id))

    private def invalidId[ID](id: ID) =
      "Invalid ID: %s".format(id)
  }
  type MongoId = MongoId.Type

}
