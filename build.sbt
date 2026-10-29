import sbt.Keys.*
import sbt.{Test, Tests}
import sbtrelease.ReleaseStateTransformations.*
import sbtversionpolicy.withsbtrelease.ReleaseVersion
import ReleaseTransformations._
import Dependencies._


// dependency versions
val contentEntityVersion = "5.0.0"
val contentAtomVersion = "15.1.0"
val storyPackageVersion = "2.2.0"
val thriftVersion = "0.24.0"
val scroogeVersion = "22.1.0" // update plugins too if this version changes
val circeVersion = "0.14.1"
val fezziwigVersion = "2.0.1"

// dependency versions (for tests only)
val scalaTestVersion = "3.2.20"
val diffsonVersion = "4.7.0"

// support non-production release types
val betaReleaseType = "beta"
val betaReleaseSuffix = "-beta.0"
val snapshotReleaseType = "snapshot"
val snapshotReleaseSuffix = "-SNAPSHOT"


lazy val artifactProductionSettings = Seq(
  scalaVersion := "2.13.14",
  // This old attempt to downgrade scrooge reserved word clashes is now insufficient... https://github.com/twitter/scrooge/issues/259#issuecomment-1900743695
  Compile / scroogeDisableStrict := true,
  // scrooge 21.3.0: Builds are now only supported for Scala 2.12+
  // https://twitter.github.io/scrooge/changelog.html#id11
  crossScalaVersions := Seq("2.12.21", scalaVersion.value),
  organization := "com.gu",
  licenses := Seq("Apache v2" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")),
  scmInfo := Some(ScmInfo(url("https://github.com/guardian/content-api-models"), "scm:git@github.com:guardian/content-api-models.git")),
  Test / testOptions +=
    Tests.Argument(TestFrameworks.ScalaTest, "-u", s"test-results/scala-${scalaVersion.value}", "-o")
)

/**
  * Root project
  */
lazy val root = Project(id = "root", base = file("."))
  .aggregate(models, json, scala)
  .settings(
    publish / skip := true,
    releaseVersion := ReleaseVersion.fromAggregatedAssessedCompatibilityWithLatestRelease().value,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      setNextVersion,
      commitNextVersion
    )
  )

/**
  * Thrift models project
  */
lazy val models = Project(id = "content-api-models", base = file("models"))
  .settings(artifactProductionSettings)
  .disablePlugins(ScroogeSBT)
  .settings(
    description := "Scala models for the Guardian's Content API",
    crossPaths := false,
    packageDoc / publishArtifact  := false,
    packageSrc / publishArtifact  := false,
    unmanagedResources / includeFilter  := "*.thrift",
    Compile / unmanagedResourceDirectories  += { baseDirectory.value / "src/main/thrift" }
  )

  /**
  * Thrift generated Scala classes project
  */
lazy val scala = Project(id = "content-api-models-scala", base = file("scala"))
  .dependsOn(models)
  .settings(artifactProductionSettings)
  .settings(
    description := "Generated classes of the Scala models for the Guardian's Content API",
    scalacOptions ++= Seq("-deprecation", "-unchecked"),
    Compile / scroogeThriftOutputFolder  := sourceManaged.value / "thrift",
    Compile / scroogeThriftSourceFolder   := baseDirectory.value / "../models/src/main/thrift",
    Compile / scroogeThriftDependencies  ++= Seq(
      "story-packages-model-thrift",
      "content-atom-model-thrift",
      "content-entity-thrift"
    ),
    // See: https://github.com/twitter/scrooge/issues/199
    Compile / scroogeThriftSources ++= {
      (Compile / scroogeUnpackDeps).value.flatMap { dir => (dir ** "*.thrift").get }
    },
    Compile / scroogePublishThrift := false,
    libraryDependencies ++= Seq(
      "org.apache.thrift" % "libthrift" % thriftVersion,
      "com.twitter" %% "scrooge-core" % scroogeVersion,
      "com.gu" % "story-packages-model-thrift" % storyPackageVersion,
      "com.gu" % "content-atom-model-thrift" % contentAtomVersion,
      "com.gu" % "content-entity-thrift" % contentEntityVersion,
      "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
    )
  )

/**
  * JSON parser project
  */
lazy val json = Project(id = "content-api-models-json", base = file("json"))
  .dependsOn(scala)
  .settings(artifactProductionSettings)
  .settings(
    description := "Json parser for the Guardian's Content API models",
    libraryDependencies ++= Seq(
      "com.gu" %% "fezziwig" % fezziwigVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "io.circe" %% "circe-optics" % circeVersion,
      "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
      "org.gnieh" %% "diffson-circe" % diffsonVersion % Test
    ),
    Compile / packageDoc / mappings := Nil
  )

lazy val benchmarks = Project(id = "benchmarks", base = file("benchmarks"))
  .dependsOn(json, scala)
  .settings(artifactProductionSettings)
  .enablePlugins(JmhPlugin)
  .settings(
    Jmh / javaOptions ++= Seq("-server", "-Xms4G", "-Xmx4G", "-XX:+UseG1GC", "-XX:-UseBiasedLocking"),
    publishArtifact := false
  )

lazy val npmPreviewReleaseTagMaybe = if (sys.env.get("RELEASE_TYPE").contains("PREVIEW_FEATURE_BRANCH")) {
  Seq(scroogeTypescriptPublishTag := "preview")
} else Seq.empty

lazy val typescript = (project in file("ts"))
  .enablePlugins(ScroogeTypescriptGen)
  .settings(artifactProductionSettings)
  .settings(npmPreviewReleaseTagMaybe)
  .settings(
    name := "content-api-models-typescript",
    scroogeTypescriptNpmPackageName := "@guardian/content-api-models",
    Compile / scroogeDefaultJavaNamespace := scroogeTypescriptNpmPackageName.value,
    Test / scroogeDefaultJavaNamespace := scroogeTypescriptNpmPackageName.value,
    description := "Typescript library built from the content api thrift definitions",
    Compile / scroogeLanguages := Seq("typescript"),
    Compile / scroogeThriftSourceFolder  := baseDirectory.value / "../models/src/main/thrift",
    scroogeTypescriptPackageLicense := "Apache-2.0",
    Compile / scroogeThriftDependencies  ++= Seq(
      "content-entity-thrift",
      "content-atom-model-thrift",
      "story-packages-model-thrift"
    ),
    scroogeTypescriptPackageMapping := Map(
      "content-entity-thrift" -> "@guardian/content-entity-model",
      "content-atom-model-thrift" -> "@guardian/content-atom-model",
      "story-packages-model-thrift" -> "@guardian/story-packages-model"
    ),
    libraryDependencies ++= Seq(
      "org.apache.thrift" % "libthrift" % thriftVersion,
      "com.twitter" %% "scrooge-core" % scroogeVersion,
      "com.gu" % "story-packages-model-thrift" % storyPackageVersion,
      "com.gu" % "content-atom-model-thrift" % contentAtomVersion,
      "com.gu" % "content-entity-thrift" % contentEntityVersion
    )
  )

val clientArtifactPrefix = "content-api-client"

lazy val clients = (project in file("clients"))
  .aggregate(client, defaultClient, catsEffectClient, firehoseClient)
  .settings(
    publish / skip := true,
  )

lazy val client = (project in file("clients/client"))
  .settings(artifactProductionSettings, clientSettings)
  .enablePlugins(BuildInfoPlugin)

lazy val defaultClient = (project in file("clients/client-default"))
  .dependsOn(client)
  .settings(artifactProductionSettings, defaultClientSettings)
  .settings(
    name := clientArtifactPrefix + "-default"
  )

lazy val catsEffectClient = (project in file("clients/client-cats-effect"))
  .dependsOn(client, defaultClient % "compile->compile;test->test")
  .settings(artifactProductionSettings, defaultClientSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.13.0",
      "org.typelevel" %% "cats-effect" % "3.7.1",
      "org.typelevel" %% "log4cats-slf4j"   % "2.8.0",
      "co.fs2" %% "fs2-core" % "3.13.0",
      "com.madgag" %% "bin-packing" % "3.0.0",
      "org.typelevel" %% "weaver-cats" % "0.13.0" % Test
    ),
    name := clientArtifactPrefix + "-cats-effect"
  )

lazy val firehoseClient = (project in file("clients/firehose-client"))
  .settings(artifactProductionSettings).settings(
    name                := "content-api-firehose-client",
    description         := "Firehose client for the CAPI Crier feed",
    libraryDependencies ++= Seq(
      capiModels,
      "com.gu" %% "thrift-serializer" % "5.0.8",
      "software.amazon.kinesis" % "amazon-kinesis-client" % "3.4.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.6",
      // "com.twitter" %% "scrooge-core" % "21.12.0",
      "at.yawk.lz4" % "lz4-java" % "1.10.4", // Necessary while the ExclusionRule for org.lz4:lz4-java is necessary
      scalaTest,
    ) ++ Seq("aws-json-protocol", "kinesis").map(artifact => "software.amazon.awssdk" % artifact % "2.57.2") ++ Seq(
      "jackson-databind", "jackson-annotations", "jackson-core"
    ).map(artifact => "com.fasterxml.jackson.core" % artifact % "2.17.3")

  )

lazy val artifactProductionSettings: Seq[Setting[?]] = Seq(
  crossScalaVersions      := scalaVersions,
  scalaVersion            := scalaVersions.max,
  scalacOptions           ++= Seq("-deprecation", "-unchecked", "-release:11"),
  licenses                := Seq(License.Apache2),
  organization            := "com.gu"
)

lazy val clientSettings: Seq[Setting[?]] = Seq(
  name                := clientArtifactPrefix,
  description         := "Scala client for the Guardian's Content API",
  buildInfoKeys       := Seq[BuildInfoKey](version),
  buildInfoPackage    := "com.gu.contentapi.buildinfo",
  buildInfoObject     := "CapiBuildInfo",
  libraryDependencies ++= clientDeps
)

lazy val defaultClientSettings: Seq[Setting[?]] = Seq(
  description         := "Default scala client for the Guardian's Content API",
  libraryDependencies ++= clientDeps ++ defaultClientDeps,
  console / initialCommands   := """
    import com.gu.contentapi.client._
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent.Await
    import scala.concurrent.duration._
  """
)

Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-u", s"test-results/scala-${scalaVersion.value}", "-o")
