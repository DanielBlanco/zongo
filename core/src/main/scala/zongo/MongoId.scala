package zongo

import mongo4cats.bson.*
import scala.util.Try
import zio.*
import zio.prelude.*

/** Mongo ID wrapper. */
object MongoId extends Subtype[ObjectId] {

  def make: MongoId = wrap(newId)

  def make[ID](id: ID): Either[String, MongoId] =
    newId(id).map(wrap)

  def zmake[ID](id: ID): IO[String, MongoId] =
    znewId(id).map(wrap)

  def toJsonMap(id: MongoId): Map[String, String] =
    Map("$oid" -> id.toString())

  def fromJsonMap(m: Map[String, String]): Either[String, MongoId] =
    m.get("$oid") match {
      case None      => Left("Not an ObjectId value")
      case Some(oid) => MongoId.make(oid)
    }

  /** This way models can just do <Model>.newId and encapsulate the Id generation
    * code that is specific to Mongo.
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

  private def znewId[ID](id: ID): IO[String, ObjectId]    =
    ZIO.fromEither(newId(id))

  private def invalidId[ID](id: ID)                       =
    "Invalid ID: %s".format(id)
}
