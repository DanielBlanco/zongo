package zongo.support

import zio.*
import zio.config.*
import zio.config.syntax.*
import zio.config.ConfigDescriptor.*
import zio.config.typesafe.*
import zio.test.*
import zio.test.Assertion.*
import zongo.*
import zongo.internal.MongoLive

trait BaseSpec extends DefaultRunnableSpec {
  override def aspects = List(TestAspect.timeout(60.seconds))

  final val TIMEOUT = 1.second
  final val TEST_DB = "zongo_test"

  // ---- ZIO Layer setup

  lazy val mongoLayer: URLayer[SpecConfig, Mongo]               =
    ZLayer.fromFunctionManaged(env => MongoLive(env.get.mongo.uri).orDie)

  lazy val specLayer: ZLayer[TestEnvironment, Nothing, SpecEnv] =
    ZLayer
      .makeSome[TestEnvironment, SpecEnv](
        Config.layer,
        mongoLayer,
        ItemsRepo.layer(TEST_DB)
      )
      .orDie

}
