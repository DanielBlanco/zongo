# ZONGO
ZIO + MongoDB thin wrapper.

## Setup

Just import the library into your Scala dependencies.

```
"dev.dblancorojas" %% "zongo" % "0.1.0"
```

## Author

Daniel Blanco Rojas [dblancorojas.dev](https://dblancorojas.dev)

## License

Zongo is provided under the [MIT license](https://github.com/DanielBlanco/zongo/blob/master/LICENSE.md).


## Notes

- Zongo is built upon the `org.mongodb.scala::mongo-scala-driver`.
- The MongoDB Scala driver is now built upon the MongoDB Reactive Streams driver
  and is an implementation of the reactive streams specification.

## About Json

Zongo's `MongoRepo` depends on 2 functions to convert to and from a bson Document:

```
    /** Converts a MongoDoc into a bson Document.
      *
      * @param d the MongoDoc (meaning the model).
      * @return the Bson Document.
      */
    def docToBson(d: D): Either[Throwable, Document]

    /** Converts a bson Document into a MongoDoc.
      *
      * @param d the Bson Document.
      * @return the MongoDoc (meaning the model).
      */
    def bsonToDoc(d: Document): Either[Throwable, D]
```

How do you implement those functions is up to you.

Here is a Circe example:

```
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import org.mongodb.scala.bson.Document
import zio.Has
import zongo._

object MongoRepoSample {

  implicit val mongoIdJsonEnc: Encoder[MongoId] =
    new Encoder[MongoId] {
      final def apply(id: MongoId): Json = Json.obj(
        ("$oid", Json.fromString(id.toString()))
      )
    }

  implicit val mongoIdJsonDec: Decoder[MongoId] =
    new Decoder[MongoId] {
      final def apply(c: HCursor): Decoder.Result[MongoId] =
        for {
          oid <- c.downField("$oid").as[String]
          id  <- MongoId.make(oid).left.map { e =>
                   DecodingFailure(e, c.history)
                 }
        } yield id
    }

  case class Item(
      _id: Option[MongoId],
      name: String
  ) extends MongoDoc

  /** Converts a MongoDoc into a bson Document.
    *
    * @param d the MongoDoc (meaning the model).
    * @return the Bson Document.
    */
  def docToBson(d: Item): Either[Throwable, Document] =
    Try(Document(d.asJson.toString)).toEither

  /** Converts a bson Document into a MongoDoc.
    *
    * @param d the Bson Document.
    * @return the MongoDoc (meaning the model).
    */
  def bsonToDoc(d: Document): Either[Throwable, Item] =
    parse(d.toJson()).flatMap(_.as[Item])
}
```
You can also check `src/test/scala/zongo/support/ItemsRepo.scala`.
