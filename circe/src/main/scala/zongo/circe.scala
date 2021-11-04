package zongo

import io.circe._
import io.circe.generic.semiauto._
import io.circe.parser._
import io.circe.syntax._

object circe {
  import mongo4cats.circe._

  implicit val encodeMongoId: Encoder[MongoId] =
    encodeObjectId.contramap[MongoId](MongoId.unwrap)

  implicit val decodeMongoId: Decoder[MongoId] =
    decodeObjectId.map(MongoId.wrap)

}
