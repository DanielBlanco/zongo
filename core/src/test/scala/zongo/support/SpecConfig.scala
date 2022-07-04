package zongo.support

import zio.*
import zio.config.*
import zio.config.syntax.*
import zio.config.ConfigDescriptor.*
import zio.config.typesafe.*
import zongo.*

/** Mongo configuration */
final case class MongoConfig(uri: MongoUri)
object MongoConfig:

  private val mongoUriDesc: ConfigDescriptor[MongoUri] =
    string.transformOrFailLeft { s =>
      MongoUri.make(s).toEither.left.map(_.mkString(","))
    }(MongoUri.unwrap)

  val desc: ConfigDescriptor[MongoConfig] =
    (nested("uri")(mongoUriDesc)).to[MongoConfig]

/** Spec configuration */
case class SpecConfig(mongo: MongoConfig)
object SpecConfig:

  private val desc: ConfigDescriptor[SpecConfig] =
    (nested("mongodb")(MongoConfig.desc)).to[SpecConfig]

  val live = TypesafeConfig.fromResourcePath(desc).mapError { e =>
    IllegalArgumentException(e.prettyPrint(','))
  }
