package mongo4cats.collection.operations

import zongo.Mongo

object FilterExt {
  implicit class FilterOps(a: Filter) {

    def translate: String =
      Mongo.bsonToBsonDocument(a.toBson).toString
  }
}
