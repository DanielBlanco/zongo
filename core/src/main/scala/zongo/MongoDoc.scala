package zongo

import mongo4cats.bson._

abstract class MongoDoc extends Product {
  def _id: Option[MongoId]
}
