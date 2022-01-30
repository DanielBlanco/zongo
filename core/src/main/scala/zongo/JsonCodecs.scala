package zongo

import mongo4cats.bson.*
import mongo4cats.codecs.MongoCodecProvider
import scala.util.Try
import zio.json.*
import zongo.MongoId

trait JsonCodecs {

  implicit val encodeObjectId: JsonEncoder[ObjectId] =
    JsonEncoder[Map[String, String]].contramap(id => Map("$oid" -> id.toHexString()))
  implicit val decodeObjectId: JsonDecoder[ObjectId] =
    JsonDecoder[Map[String, String]].mapOrFail {
      _.get("$oid") match {
        case None      =>
          Left("Not an ObjectId value")
        case Some(oid) =>
          Try(new ObjectId(oid)).toEither.left.map(_.getMessage())
      }
    }

  implicit val encodeMongoId: JsonEncoder[MongoId] =
    JsonEncoder[ObjectId].contramap(MongoId.unwrap)
  implicit val decodeMongoId: JsonDecoder[MongoId] =
    JsonDecoder[ObjectId].mapOrFail(MongoId.make(_))

}
