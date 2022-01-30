package zongo.support

import zio.*
import zio.config.*
import zio.config.syntax.*
import zio.config.ConfigDescriptor.*
import zio.config.typesafe.*
import zio.clock.*
import zio.duration.*
import zio.magic.*
import zio.test.*
import zio.test.Assertion.*
import zio.test.environment.*
import zongo.*

trait BaseSpec extends DefaultRunnableSpec {
  override def aspects = List(TestAspect.timeout(60.seconds))

  final val TIMEOUT = 1.second
  final val TEST_DB = "zongo_test"

  // ---- Config setup

  case class SpecConfig(mongo: MongoConfig)
  case class MongoConfig(uri: MongoUri)

  object Config {

    val mongoUriDesc: ConfigDescriptor[MongoUri]            =
      string.transformOrFailLeft { s =>
        MongoUri.make(s).toEither.leftMap(_.mkString(","))
      }(MongoUri.unwrap)

    private val mongoCfgDesc: ConfigDescriptor[MongoConfig] =
      (
        nested("uri")(mongoUriDesc)
      )(MongoConfig.apply, MongoConfig.unapply)

    private val cfgDesc: ConfigDescriptor[SpecConfig]       =
      (
        nested("mongodb")(mongoCfgDesc)
      )(SpecConfig.apply, SpecConfig.unapply)

    val layer = TypesafeConfig.fromDefaultLoader(cfgDesc)
  }

  // ---- ZIO Layer setup

  type SpecEnv = Has[SpecConfig] with Mongo with ItemsRepo

  lazy val mongoLayer: RLayer[Has[SpecConfig], Mongo]           =
    for {
      cfg   <- ZLayer.service[SpecConfig].map(_.get)
      mongo <- Mongo.live(cfg.mongo.uri)
    } yield mongo

  lazy val specLayer: ZLayer[TestEnvironment, Nothing, SpecEnv] =
    ZLayer
      .fromSomeMagic[TestEnvironment, SpecEnv](
        Config.layer,
        mongoLayer,
        ItemsRepo.layer(TEST_DB)
      )
      .orDie

  // ---- Helper functions

  // protected def clearDB = Mongo.clearDatabase.orDie
}
