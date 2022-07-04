ThisBuild / scalaVersion           := "2.13.6"
ThisBuild / organization           := "dev.dblancorojas"
ThisBuild / organizationName       := "Daniel Blanco Rojas"
ThisBuild / versionScheme          := Some("early-semver")
ThisBuild / versionPolicyIntention := Compatibility.BinaryAndSourceCompatible

lazy val noPublish      = Seq(
  publish         := {},
  publishLocal    := {},
  publishArtifact := false,
  publish / skip  := true
)

lazy val commonSettings = Seq(
  organizationName               := "MongoDB client wrapper for ZIO",
  startYear                      := Some(2021),
  licenses                       += ("Apache-2.0", new URL(
    "https://www.apache.org/licenses/LICENSE-2.0.txt"
  )),
  resolvers                      += "Apache public" at "https://repository.apache.org/content/groups/public/",
  scalafmtOnCompile              := true,
  Compile / scalacOptions       ++= Seq(
    "-feature",
    "-encoding",
    "UTF-8",
    "-deprecation",
    "-Xfatal-warnings",
    "-Ymacro-annotations",
    "-Xsource:3"
  ),
  Compile / doc / scalacOptions ++= Seq(
    "-no-link-warnings" // Suppresses problems with Scaladoc links
  ),
  libraryDependencies           ++= Dependencies.CompilerPlugins.libs,
  testFrameworks                 := Seq(Dependencies.Testing.framework)
)

lazy val root           = project
  .in(file("."))
  .settings(noPublish)
  .settings(name := "zongo")
  .aggregate(
    `zongo-core`,
    `zongo-circe`
  )

lazy val `zongo-core`   = project
  .in(file("core"))
  .settings(commonSettings)
  .settings(
    name                     := "zongo-core",
    libraryDependencies     ++= Dependencies.core,
    Test / parallelExecution := false,
    Test / fork              := true
  )

lazy val `zongo-circe`  = project
  .in(file("circe"))
  .dependsOn(`zongo-core`)
  .settings(commonSettings)
  .settings(
    name                     := "zongo-circe",
    libraryDependencies     ++= Dependencies.circe,
    Test / parallelExecution := false,
    Test / fork              := true
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
