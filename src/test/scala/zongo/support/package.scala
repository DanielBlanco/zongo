package zongo

import io.circe._
import io.circe.generic.semiauto._
import io.circe.parser._
import io.circe.syntax._
import zio.Has

package object support {
  type ItemsRepo = Has[ItemsRepo.Service]

  implicit val mongoIdJsonEnc: Encoder[MongoId] =
    new Encoder[MongoId] {
      final def apply(id: MongoId): Json = Json.obj(
        ("$oid", Json.fromString(id.toString()))
      )
    }

  implicit val mongoIdJsonDec: Decoder[MongoId] =
    new Decoder[MongoId] {
      final def apply(c: HCursor): Decoder.Result[MongoId] =
        for {
          oid <- c.downField("$oid").as[String]
          id  <- MongoId.make(oid).left.map { e =>
                   DecodingFailure(e, c.history)
                 }
        } yield id
    }
}
