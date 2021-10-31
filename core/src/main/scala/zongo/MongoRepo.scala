package zongo

import com.mongodb.client.result._
import mongo4cats.collection.operations._
import zio._
import zio.interop.catz._
import zio.stream._

/** Helper service to facilitate the creation of Mongo repositories. */
object MongoRepo {

  trait Service[D <: MongoDoc] {

    /** The mongo collection. */
    val databaseName: String

    /** The mongo collection. */
    val collectionName: String

    /** Clears the data from this collection. */
    def clearCollection: Task[DeleteResult]

    /** Count all the documents in this collection.
      *
      * @return a Task containing the number (Long) of documents found.
      */
    def count: Task[Long]

    /** Count all the documents in this collection.
      *
      * @param query the bson document to filter by.
      * @return a Task containing the number (Long) of documents found.
      */
    def count(filter: Filter): Task[Long]

    /** Returns a query builder to find documents matching some criteria. */
    def find: Task[FindQueryBuilder[D]]

    /** Inserts the provided document.
      *
      * @param doc the document to insert.
      * @return
      */
    def insert(doc: D): Task[InsertOneResult]

    /** Inserts a chunk of documents. The preferred way to perform bulk inserts
      *  is to use the BulkWrite API.
      *
      *  However, when talking with a server &lt; 2.6, using this method will be
      *  faster due to constraints in the bulk API related to error handling.
      *
      * @param docs the documents to insert
      * @return a Task[InsertManyResult]
      */
    def insertMany(docs: Chunk[D]): Task[InsertManyResult]

    /** Deletes the documents that match the provided query.
      *
      * @param query to filter by.
      * @return the DeleteResult.
      */
    def remove(filter: Filter): Task[DeleteResult]

    /** Alias for clearCollection */
    def removeAll = clearCollection

    /** Updates records in database.
      *
      * @example
      * {{{
      *   UserRepo.update(
      *     Filters.eq("name", "Liz"),
      *     Updates.set("name", "Lizzy")
      *   )
      * }}}
      *
      * @param query How to find the records to update (selector).
      * @param update The bson values to update (update).
      * @return the update result
      */
    def update(filter: Filter, update: Update): Task[UpdateResult]
  }
}
