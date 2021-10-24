// package zongo.support

// import io.circe._
// import io.circe.generic.semiauto._
// import io.circe.parser._
// import io.circe.syntax._
// import org.mongodb.scala.{Document => _, _}
// import org.mongodb.scala.bson._
// import org.mongodb.scala.result._
// import scala.reflect.ClassTag
// import scala.util.Try
// import zio._
// import zio.macros._
// import zio.stream._
// import zongo._
// import zongo.internal.MongoRepoLive
// import org.bson.codecs.configuration.CodecProvider

// case class Item(
//     _id: Option[MongoId],
//     name: String
// ) extends MongoDoc
// object Item {
//   implicit val jsonDecoder: Decoder[Item] = deriveDecoder[Item]
//   implicit val jsonEncoder: Encoder[Item] = deriveEncoder[Item]
// }

// @accessible
// object ItemsRepo {
//   trait Service extends MongoRepo.Service[Item] {
//     implicit val ct: ClassTag[Item] = ClassTag(classOf[Item])

//     override val collectionName = "items"

//     /** definition to get @accessible functions */
//     def count: Task[Long]

//     /** definition to get @accessible functions */
//     def count(query: conversions.Bson): Task[Long]

//     /** definition to get @accessible functions */
//     def find(
//         query: conversions.Bson,
//         sorts: Option[conversions.Bson] = None,
//         limit: Option[Int] = None,
//         skip: Option[Int] = None,
//         projection: Option[conversions.Bson] = None
//     ): Stream[MongoException, Item]

//     /** definition to get @accessible functions */
//     def insertMany(docs: Seq[Item]): Task[InsertManyResult]

//     /** definition to get @accessible functions */
//     def remove(query: conversions.Bson): IO[MongoException, Unit]

//     /** definition to get @accessible functions */
//     def removeAll: Task[Unit]

//     /** definition to get @accessible functions */
//     def update(
//         query: conversions.Bson,
//         update: conversions.Bson
//     ): Task[UpdateResult]
//   }

//   case class Live(
//       mongo: Mongo.Service,
//       databaseName: String
//   ) extends MongoRepoLive[Item](mongo)
//       with Service {

//     /** Converts a MongoDoc into a bson Document.
//       *
//       * @param d the MongoDoc (meaning the model).
//       * @return the Bson Document.
//       */
//     def docToBson(d: Item): Either[Throwable, Document] =
//       Try(Document(d.asJson.toString)).toEither

//     /** Converts a bson Document into a MongoDoc.
//       *
//       * @param d the Bson Document.
//       * @return the MongoDoc (meaning the model).
//       */
//     def bsonToDoc(d: Document): Either[Throwable, Item] =
//       parse(d.toJson()).flatMap(_.as[Item])

//   }

//   def layer(db: String): URLayer[Mongo, ItemsRepo] =
//     ZLayer.fromService[Mongo.Service, Service](Live(_, db))
// }
