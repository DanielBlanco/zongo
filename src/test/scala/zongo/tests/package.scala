package zongo

package object tests {

  final val DB = "zongo_test"

  final val COLL_1 = "samples1"
  final val COLL_2 = "samples2"
  // final val OBJ_ID_1 = Mongo.newId

  // val bsonObj1 = Bson.obj("_id" -> OBJ_ID_1)

  // val bsonObjAB = Bson.obj(
  //   "_id"      -> OBJ_ID_1,
  //   "name"     -> "A",
  //   "lastName" -> "B"
  // )

  // def bulkInsertData =
  //   Seq(
  //     Bson.obj("_id" -> Mongo.newId, "num" -> 1, "g" -> 1),
  //     Bson.obj("_id" -> Mongo.newId, "num" -> 2, "g" -> 1),
  //     Bson.obj("_id" -> Mongo.newId, "num" -> 3, "g" -> 1)
  //   )
}
