package zongo

import zio._
import zio.prelude._
import zio.test.Assertion.{hasSizeString, isLessThan}

/** Extends SubtypeSmart to override make and trim the string value.
  *
  * @param assertion
  *   required by SubtypeSmart
  */
abstract class SubtypeTrimmedString(assertion: zio.test.Assertion[String])
    extends SubtypeSmart[String](assertion) {
  override def make(value: String) = super.make(value.trim)
}

/** New MongoUri type.
  */
object MongoUri extends SubtypeTrimmedString(nonEmpty)
