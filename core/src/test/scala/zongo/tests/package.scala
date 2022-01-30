// package zongo

// import org.mongodb.scala.bson.Document

// package object tests {

//   final val DB     = "zongo_test"
//   final val COLL_1 = "samples1"
//   final val COLL_2 = "samples2"

//   private[tests] def collection1 =
//     getCollection(COLL_1)

//   private[tests] def collection2 =
//     getCollection(COLL_2)

//   private[tests] def getCollection(name: String) =
//     for {
//       db <- Mongo.database(DB)
//       c  <- Mongo.collection[Document](name)(db)
//     } yield c

//   // final val OBJ_ID_1 = Mongo.newId

//   // val bsonObj1 = Bson.obj("_id" -> OBJ_ID_1)

//   // val bsonObjAB = Bson.obj(
//   //   "_id"      -> OBJ_ID_1,
//   //   "name"     -> "A",
//   //   "lastName" -> "B"
//   // )

//   def bulkInsertData =
//     Seq(
//       Document("_id" -> MongoId.make, "num" -> 1, "g" -> 1),
//       Document("_id" -> MongoId.make, "num" -> 2, "g" -> 1),
//       Document("_id" -> MongoId.make, "num" -> 3, "g" -> 1),
//       Document("_id" -> MongoId.make, "num" -> 4, "g" -> 1, "opt" -> None)
//     )
// }
