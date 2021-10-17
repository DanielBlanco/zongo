package zongo

import org.mongodb.scala.bson._
import org.mongodb.scala.bson.codecs.Macros
import org.bson.codecs.configuration.CodecProvider

abstract class MongoDoc extends Product {
  def _id: Option[MongoId]
}
