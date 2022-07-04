package zongo

import zio.*
import zio.prelude.*
import zio.prelude.Assertion.*

/** New MongoUri type. */
object MongoUri extends Subtype[String]:

  override inline def assertion =
    startsWith("mongodb")

type MongoUri = MongoUri.Type
