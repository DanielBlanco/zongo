package zongo

import zio.*
import zio.prelude.*

/** New MongoUri type. */
object MongoUri extends Subtype[String] {
  def assertion = assert {
    Assertion.hasLength(Assertion.greaterThan(0))
  }
}
