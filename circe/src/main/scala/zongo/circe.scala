package zongo

import io.circe.*
import io.circe.generic.semiauto.*
import io.circe.parser.*
import io.circe.syntax.*

object circe {
  import mongo4cats.circe.*

  implicit val encodeMongoId: Encoder[MongoId] =
    encodeObjectId.contramap[MongoId](MongoId.unwrap)

  implicit val decodeMongoId: Decoder[MongoId] =
    decodeObjectId.map(MongoId.wrap)
}
