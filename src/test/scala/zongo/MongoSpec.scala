package zongo

import support._
import tests._
import zio.duration._
import zio.test._
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test.environment._

object MongoSpec extends BaseSpec {

  def spec = // @@ before(clearDB)
    (suite("MongoSpec")(tests: _*) @@ sequential)
      .provideCustomLayerShared(specLayer)

  def tests =
    CountTests.tests ++
      IndexesTests.tests ++
      FindTests.tests ++
      UpdateTests.tests ++
      OtherTests.tests
}
