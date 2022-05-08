package zongo.support

import zio.*
import zio.config.*
import zio.config.syntax.*
import zio.config.ConfigDescriptor.*
import zio.config.typesafe.*
import zongo.*

type SpecEnv = SpecConfig with Mongo with ItemsRepo

// ---- Config setup

case class SpecConfig(mongo: MongoConfig)
object SpecConfig:
  def values(cfg: SpecConfig): Option[MongoConfig] =
    Some(cfg.mongo)

case class MongoConfig(uri: MongoUri)
object MongoConfig:
  def values(cfg: MongoConfig): Option[MongoUri] =
    Some(cfg._1)

object Config:

  val mongoUriDesc: ConfigDescriptor[MongoUri]            =
    string.transformOrFailLeft { s =>
      MongoUri.make(s).toEither.left.map(_.mkString(","))
    }(MongoUri.unwrap)

  private val mongoCfgDesc: ConfigDescriptor[MongoConfig] =
    nested("uri")(mongoUriDesc).to[MongoConfig]

  private val cfgDesc: ConfigDescriptor[SpecConfig]       =
    nested("mongodb")(mongoCfgDesc).to[SpecConfig]

  val layer = TypesafeConfig.fromResourcePath(cfgDesc)
