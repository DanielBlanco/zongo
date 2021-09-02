import Dependencies._

ThisBuild / scalaVersion     := "2.13.6"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "dev.dblancorojas"
ThisBuild / organizationName := "Daniel Blanco Rojas"

lazy val root = (project in file("."))
  .settings(
    name                 := "zongo",
    scalacOptions       ++= Seq(
      "-encoding",
      "UTF-8",
      "-deprecation",
      "-Xfatal-warnings",
      "-Ymacro-annotations"
    ),
    libraryDependencies ++= (
      CompilerPlugins.libs ++ MongoDB.libs ++ Testing.libs ++ ZIO.libs
    ),
    testFrameworks       := Seq(Testing.framework),
    Test / fork          := true
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
