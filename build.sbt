import com.twitter.scrooge.ScroogeSBT._
import sbt.Keys._
import sbtrelease.ReleaseStateTransformations._
import com.twitter.scrooge.ScroogeSBT

val mavenSettings = Seq(
  pomExtra := (
    <url>https://github.com/guardian/content-api-models</url>
    <scm>
      <connection>scm:git:git@github.com:guardian/content-api-models.git</connection>
      <developerConnection>scm:git:git@github.com:guardian/content-api-models.git</developerConnection>
      <url>git@github.com:guardian/content-api-models.git</url>
    </scm>
    <developers>
      <developer>
        <id>cb372</id>
        <name>Chris Birchall</name>
        <url>https://github.com/cb372</url>
      </developer>
      <developer>
        <id>mchv</id>
        <name>Mariot Chauvin</name>
        <url>https://github.com/mchv</url>
      </developer>
      <developer>
        <id>LATaylor-guardian</id>
        <name>Luke Taylor</name>
        <url>https://github.com/LATaylor-guardian</url>
      </developer>
    </developers>
  ),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false }
)

val commonSettings = Seq(
  scalaVersion := "2.11.8",
  crossPaths := false,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  organization := "com.gu",
  licenses := Seq("Apache v2" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))
) ++ mavenSettings

val circeVersion = "0.5.2"

/**
  * Root project
  */
lazy val root = Project(id = "root", base = file("."))
  .settings(commonSettings)
  .aggregate(models, json, macros, scala)
  .settings(
    publishArtifact := false,
    releaseProcess := Seq(
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      publishArtifacts,
      setNextVersion,
      commitNextVersion,
      releaseStepCommand("sonatypeReleaseAll"),
      pushChanges
    )
  )

lazy val macros = Project(id = "content-api-models-macros", base = file("macros"))
  .settings(commonSettings)
  .settings(
    description := "Macros",
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "org.apache.thrift" % "libthrift" % "0.9.1",
      "com.twitter" %% "scrooge-core" % "4.5.0",
      "org.apache.commons" % "commons-lang3" % "3.4"
    )
  )

/**
  * Thrift models project
  */
lazy val models = Project(id = "content-api-models", base = file("models"))
  .settings(commonSettings)
  .disablePlugins(ScroogeSBT)
  .settings(
    resolvers += Resolver.sonatypeRepo("releases"),
    description := "Scala models for the Guardian's Content API",
    unmanagedResourceDirectories in Compile += { baseDirectory.value / "src/main/thrift" },
    libraryDependencies ++= Seq(
      "com.gu" % "story-packages-model-thrift" % "1.0.3",
      "com.gu" % "content-atom-model-thrift" % "2.4.20",
      "com.gu" % "content-entity-thrift" % "0.1.0"
    )
  )

  /**
  * Thrift generated Scala classes project
  */
lazy val scala = Project(id = "content-api-models-scala", base = file("scala"))
  .dependsOn(models)
  .settings(commonSettings)
  .settings(
    description := "Generated classes of the Scala models for the Guardian's Content API",
    javacOptions ++= Seq("-source", "1.7", "-target", "1.7"),
    scalacOptions ++= Seq("-deprecation", "-unchecked"),
    scroogeThriftOutputFolder in Compile := sourceManaged.value / "thrift",
    scroogeThriftSourceFolder in Compile := baseDirectory.value / "../models/src/main/thrift",
    scroogeThriftDependencies in Compile ++= Seq(
      "content-api-models",
      "story-packages-model-thrift",
      "content-atom-model-thrift",
      "content-entity-thrift"
    ),
    // See: https://github.com/twitter/scrooge/issues/199
    scroogeThriftSources in Compile ++= {
      (scroogeUnpackDeps in Compile).value.flatMap { dir => (dir ** "*.thrift").get }
    },
    libraryDependencies ++= Seq(
      "org.apache.thrift" % "libthrift" % "0.9.1",
      "com.twitter" %% "scrooge-core" % "4.5.0"
    )
  )

/**
  * JSON parser project
  */
lazy val json = Project(id = "content-api-models-json", base = file("json"))
  .dependsOn(scala % "provided", macros)
  .settings(commonSettings)
  .settings(
    description := "Json parser for the Guardian's Content API models",
    libraryDependencies ++= Seq(
      "joda-time" % "joda-time" % "2.3",
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "io.circe" %% "circe-optics" % circeVersion,
      "com.github.agourlay" %% "cornichon" % "0.9.1" % "test",
      "org.scalatest" %% "scalatest" % "2.2.1" % "test",
      "com.google.guava" % "guava" % "19.0" % "test"
    ),
    mappings in (Compile, packageDoc) := Nil
  )

lazy val benchmarks = Project(id = "benchmarks", base = file("benchmarks"))
  .dependsOn(json, scala)
  .settings(commonSettings)
  .enablePlugins(JmhPlugin)
  .settings(
    libraryDependencies += "com.google.guava" % "guava" % "19.0",
    javaOptions in Jmh ++= Seq("-server", "-Xms4G", "-Xmx4G", "-XX:+UseG1GC", "-XX:-UseBiasedLocking"),
    publishArtifact := false
  )
