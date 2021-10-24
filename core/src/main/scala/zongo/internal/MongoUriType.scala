package zongo.internal

import zio._
import zio.prelude._
import zio.test.Assertion.{hasSizeString, isLessThan}

trait MongoUriType {

  lazy val nonEmpty = hasSizeString(isGreaterThan(0))

  /** Extends SubtypeSmart to override make and trim the string value.
    *
    * @param assertion required by SubtypeSmart
    */
  abstract class SubtypeTrimmedString(assertion: zio.test.Assertion[String])
      extends SubtypeSmart[String](assertion) {
    override def make(value: String) = super.make(value.trim)
  }

  /** New MongoUri type.
    */
  object MongoUri extends SubtypeTrimmedString(nonEmpty)
  type MongoUri = MongoUri.Type
}
