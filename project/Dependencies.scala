import sbt._

object Dependencies {
  object V {
    val betterMonadicFor = "0.3.1"
    val mongo            = "4.2.3"
    val circe            = "0.14.1"
    val zio              = "1.0.10"
    val zioCats          = "2.4.0.0"
    val zioConfig        = "1.0.4"
    val zioLogging       = "0.5.8"
    val zioMagic         = "0.2.6"
    val zioReactStreams  = "1.3.4"
    val zioPrelude       = "1.0.0-RC6"
  }

  object CompilerPlugins {
    val libs = Seq(
      compilerPlugin("com.olegpy" %% "better-monadic-for" % V.betterMonadicFor)
    )
  }

  /** Used in RepoTests */
  object Circe {
    val libs = Seq(
      "io.circe" %% "circe-core"    % V.circe,
      "io.circe" %% "circe-generic" % V.circe,
      "io.circe" %% "circe-parser"  % V.circe
    )
  }

  object MongoDB {
    val libs = Seq(
      "org.mongodb.scala" %% "mongo-scala-bson"   % V.mongo,
      "org.mongodb.scala" %% "mongo-scala-driver" % V.mongo
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
      "dev.zio"              %% "zio-macros"                  % V.zio,
      "dev.zio"              %% "zio-prelude"                 % V.zioPrelude,
      "io.github.kitlangton" %% "zio-magic"                   % V.zioMagic
    )
  }
}
