package zongo.support

import zio._
import zio.config._
import zio.config.syntax._
import zio.config.ConfigDescriptor._
import zio.config.typesafe._
import zio.clock._
import zio.duration._
import zio.magic._
import zio.test._
import zio.test.Assertion._
import zio.test.environment._
import zongo._

trait BaseSpec extends DefaultRunnableSpec {
  override def aspects = List(TestAspect.timeout(60.seconds))

  final val TIMEOUT = 1.second
  final val TEST_DB = "zongo_test"

  // ---- Config setup

  case class SpecConfig(mongo: MongoConfig)
  case class MongoConfig(uri: MongoUri)

  object Config {

    val mongoUriDesc: ConfigDescriptor[MongoUri] =
      string.transformOrFailLeft { s =>
        MongoUri.make(s).toEither.leftMap(_.mkString(","))
      }(MongoUri.unwrap)

    private val mongoCfgDesc: ConfigDescriptor[MongoConfig] =
      (
        nested("uri")(mongoUriDesc)
      )(MongoConfig.apply, MongoConfig.unapply)

    private val cfgDesc: ConfigDescriptor[SpecConfig] =
      (
        nested("mongodb")(mongoCfgDesc)
      )(SpecConfig.apply, SpecConfig.unapply)

    val layer = TypesafeConfig.fromDefaultLoader(cfgDesc)
  }

  // ---- ZIO Layer setup

  type SpecEnv = Has[SpecConfig] with Mongo with ItemsRepo

  lazy val mongoLayer: RLayer[Has[SpecConfig], Mongo] =
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
