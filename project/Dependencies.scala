import sbt._

object Dependencies {
  object V {
    val betterMonadicFor = "0.3.1"
    val mongo4cats       = "0.4.0"
    val circe            = "0.14.1"
    val zio              = "1.0.12"
    val zioCats          = "3.2.9.0"
    val zioConfig        = "1.0.10"
    val zioJson          = "0.1.5"
    val zioLogging       = "0.5.14"
    val zioMagic         = "0.3.11"
    val zioPrelude       = "1.0.0-RC8"
    val zioReactStreams  = "1.3.8"
  }

  object CompilerPlugins {
    val libs = Seq(
      compilerPlugin("com.olegpy" %% "better-monadic-for" % V.betterMonadicFor)
    )
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
      "dev.zio"              %% "zio"                         % V.zio,
      "dev.zio"              %% "zio-streams"                 % V.zio,
      "dev.zio"              %% "zio-interop-cats"            % V.zioCats,
      "dev.zio"              %% "zio-interop-reactivestreams" % V.zioReactStreams,
      "dev.zio"              %% "zio-config"                  % V.zioConfig,
      "dev.zio"              %% "zio-config-typesafe"         % V.zioConfig,
      "dev.zio"              %% "zio-logging"                 % V.zioLogging,
      "dev.zio"              %% "zio-logging-slf4j"           % V.zioLogging,
      "dev.zio"              %% "zio-json"                    % V.zioJson,
      "dev.zio"              %% "zio-macros"                  % V.zio,
      "dev.zio"              %% "zio-prelude"                 % V.zioPrelude,
      "io.github.kitlangton" %% "zio-magic"                   % V.zioMagic
    )
  }

  def core =
    MongoDB.libs ++ Testing.libs ++ ZIO.libs

  def circe =
    Circe.libs ++ Testing.libs
}
