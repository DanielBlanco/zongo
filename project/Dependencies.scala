import sbt._

object Dependencies {
  object V {
    val mongo4cats      = "0.4.8"
    val circe           = "0.14.2"
    val zio             = "2.0.0"
    val zioCats         = "3.3.0"
    val zioConfig       = "3.0.1"
    val zioJson         = "0.3.0-RC9"
    val zioLogging      = "2.0.0"
    val zioMagic        = "0.3.11"
    val zioPrelude      = "1.0.0-RC15"
    val zioReactStreams = "2.0.0"
  }

  /** Used in RepoTests */
  object Circe {
    val libs = Seq(
      "io.circe"           %% "circe-core"       % V.circe,
      "io.circe"           %% "circe-generic"    % V.circe,
      "io.circe"           %% "circe-parser"     % V.circe,
      "io.github.kirill5k" %% "mongo4cats-circe" % V.mongo4cats
    )
  }

  object MongoDB {
    val libs = Seq(
      "io.github.kirill5k" %% "mongo4cats-core" % V.mongo4cats
      // "org.mongodb.scala" %% "mongo-scala-bson"   % V.mongo,
      // "org.mongodb.scala" %% "mongo-scala-driver" % V.mongo
    )
  }

  object Testing {
    val libs = Seq(
      "dev.zio" %% s"zio-test"     % V.zio % Test,
      "dev.zio" %% s"zio-test-sbt" % V.zio % Test
    )

    val framework = new TestFramework("zio.test.sbt.ZTestFramework")
  }

  object ZIO {
    val libs = Seq(
      "dev.zio" %% "zio"                         % V.zio,
      "dev.zio" %% "zio-interop-cats"            % V.zioCats,
      "dev.zio" %% "zio-interop-reactivestreams" % V.zioReactStreams,
      "dev.zio" %% "zio-config"                  % V.zioConfig,
      "dev.zio" %% "zio-config-typesafe"         % V.zioConfig,
      "dev.zio" %% "zio-logging"                 % V.zioLogging,
      "dev.zio" %% "zio-logging-slf4j"           % V.zioLogging,
      "dev.zio" %% "zio-json"                    % V.zioJson,
      "dev.zio" %% "zio-prelude"                 % V.zioPrelude
    )
  }

  def core =
    MongoDB.libs ++ Testing.libs ++ ZIO.libs

  def circe =
    Circe.libs ++ Testing.libs
}
