package zongo

import zio.*
import zio.prelude.*

/** New MongoUri type. */
object MongoUri extends Subtype[String] {
  override def assertion =
    Assertion.hasLength(Assertion.greaterThan(0))
}
