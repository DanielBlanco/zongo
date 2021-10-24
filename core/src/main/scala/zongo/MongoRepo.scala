// package zongo

// import org.mongodb.scala.{Document => _, _}
// import org.mongodb.scala.bson._
// import org.mongodb.scala.model.{IndexModel, IndexOptions, Updates}
// import org.mongodb.scala.result._
// import scala.reflect.ClassTag
// import zio._
// import zio.stream._

// /** Helper service to facilitate the creation of Mongo repositories. */
// object MongoRepo {
//   trait Service[D <: MongoDoc] {

//     /** The mongo collection. */
//     val databaseName: String

//     /** The mongo collection. */
//     val collectionName: String

//     /** Clears the data from a this collection. */
//     def clearCollection: Task[Unit]

//     /** Count all the documents in this collection.
//       *
//       * @return a Task containing the number (Long) of documents found.
//       */
//     def count: Task[Long]

//     /** Count all the documents in this collection.
//       *
//       * @param query the bson document to filter by.
//       * @return a Task containing the number (Long) of documents found.
//       */
//     def count(query: conversions.Bson): Task[Long]

//     /** Converts a MongoDoc into a bson Document.
//       *
//       * @param d the MongoDoc (meaning the model).
//       * @return the Bson Document.
//       */
//     def docToBson(d: D): Either[Throwable, Document]

//     /** Converts a bson Document into a MongoDoc.
//       *
//       * @param d the Bson Document.
//       * @return the MongoDoc (meaning the model).
//       */
//     def bsonToDoc(d: Document): Either[Throwable, D]

//     /** Find the documents matching the given criteria.
//       *
//       * @param query the bson document to filter by.
//       * @param sorts the bson document to sort by (optional).
//       * @param limit maximum number of records to return (optional).
//       * @param skip number of records to skip (optional).
//       * @param projection to select only the necessary data (optional).
//       * @return a Task containing a Seq of BsonValue.
//       */
//     def find(
//         query: conversions.Bson,
//         sorts: Option[conversions.Bson] = None,
//         limit: Option[Int] = None,
//         skip: Option[Int] = None,
//         projection: Option[conversions.Bson] = None
//     ): Stream[MongoException, D]

//     /** Inserts the provided document.
//       *
//       * @param doc the document to insert.
//       * @return
//       */
//     def insert(doc: D): Task[InsertOneResult]

//     /** Inserts a batch of documents. The preferred way to perform bulk inserts
//       *  is to use the BulkWrite API.
//       *
//       *  However, when talking with a server &lt; 2.6, using this method will be
//       *  faster due to constraints in the bulk API related to error handling.
//       *
//       * @param docs the documents to insert
//       * @return a Task[InsertManyResult]
//       */
//     def insertMany(docs: Seq[D]): Task[InsertManyResult]

//     /** Deletes the documents that match the provided query.
//       *
//       * @param query to filter by.
//       * @return nothing useful.
//       */
//     def remove(query: conversions.Bson): IO[MongoException, Unit]

//     /** Alias for clearCollection */
//     def removeAll = clearCollection

//     /** Updates records in database.
//       *
//       * @example
//       * {{{
//       *   UserRepo.update(
//       *     Filters.eq("name", "Liz"),
//       *     Updates.set("name", "Lizzy")
//       *   )
//       * }}}
//       *
//       * @param query How to find the records to update (selector).
//       * @param update The bson values to update (update).
//       * @return the update result
//       */
//     def update(
//         query: conversions.Bson,
//         update: conversions.Bson
//     ): Task[UpdateResult]
//   }
// }
