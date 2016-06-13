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

/**
  * Root project
  */
lazy val root = Project(id = "root", base = file("."))
  .settings(commonSettings)
  .aggregate(models, json)
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

/**
  * Thrift models project
  */
lazy val models = Project(id = "content-api-models", base = file("models"))
  .settings(commonSettings)
  .disablePlugins(ScroogeSBT)
  .settings(
    description := "Scala models for the Guardian's Content API",
    unmanagedResourceDirectories in Compile += { baseDirectory.value / "src/main/thrift" },
    libraryDependencies ++= Seq(
      "com.gu" % "story-packages-model-thrift" % "1.0.3",
      "com.gu" % "content-atom-model-thrift" % "1.0.1"
    )
  )

/**
  * JSON parser project
  */
lazy val json = Project(id = "content-api-models-json", base = file("json"))
  .dependsOn(models)
  .settings(commonSettings)
  .settings(
    description := "Json parser for the Guardian's Content API models",
    javacOptions ++= Seq("-source", "1.7", "-target", "1.7"),
    scalacOptions ++= Seq("-deprecation", "-unchecked"),

    scroogeThriftOutputFolder in Compile := sourceManaged.value / "thrift",
    scroogeThriftSourceFolder in Compile := baseDirectory.value / "../models/src/main/thrift",
    scroogeThriftDependencies in Compile ++= Seq(
      "content-api-models",
      "story-packages-model-thrift",
      "content-atom-model-thrift"
    ),
    // See: https://github.com/twitter/scrooge/issues/199
    scroogeThriftSources in Compile ++= {
      (scroogeUnpackDeps in Compile).value.flatMap { dir => (dir ** "*.thrift").get }
    },

    libraryDependencies ++= Seq(
      "org.apache.thrift" % "libthrift" % "0.9.1",
      "com.twitter" %% "scrooge-core" % "4.5.0",
      "org.json4s" %% "json4s-jackson" % "3.3.0",
      "org.json4s" %% "json4s-ext" % "3.3.0",
      "joda-time" % "joda-time" % "2.3",
      "org.scalatest" %% "scalatest" % "2.2.1" % "test",
      "com.google.guava" % "guava" % "19.0" % "test"
    ),
    mappings in (Compile, packageBin) ~= { _.filter { case (file, toPath) => file.getAbsolutePath.contains("com/gu/contentapi") } },
    mappings in (Compile, packageDoc) := Nil
  )
