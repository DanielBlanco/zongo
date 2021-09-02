import zio._
import zio.prelude._
import zongo.internal._

package object zongo extends MongoIdType with MongoUriType {
  type Mongo = Has[Mongo.Service]
}
