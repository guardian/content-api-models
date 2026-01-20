import sbt.Keys.*
import sbt.{Test, Tests}
import sbtrelease.ReleaseStateTransformations.*
import sbtversionpolicy.withsbtrelease.ReleaseVersion

// dependency versions
val contentEntityVersion = "4.0.0"
val contentAtomVersion = "9.0.0"
val storyPackageVersion = "2.2.0"
val thriftVersion = "0.15.0"
val scroogeVersion = "22.1.0" // update plugins too if this version changes
val circeVersion = "0.14.1"
val fezziwigVersion = "2.0.0"

// dependency versions (for tests only)
val scalaTestVersion = "3.0.9"
val guavaVersion = "19.0"
val diffsonVersion = "4.1.1"

// support non-production release types
val betaReleaseType = "beta"
val betaReleaseSuffix = "-beta.0"
val snapshotReleaseType = "snapshot"
val snapshotReleaseSuffix = "-SNAPSHOT"


lazy val artifactProductionSettings = Seq(
  scalaVersion := "2.13.12",
  // This old attempt to downgrade scrooge reserved word clashes is now insufficient... https://github.com/twitter/scrooge/issues/259#issuecomment-1900743695
  Compile / scroogeDisableStrict := true,
  // scrooge 21.3.0: Builds are now only supported for Scala 2.12+
  // https://twitter.github.io/scrooge/changelog.html#id11
  crossScalaVersions := Seq("2.12.18", scalaVersion.value),
  organization := "com.gu",
  licenses := Seq("Apache v2" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")),
  resolvers ++= Resolver.sonatypeOssRepos("public"),
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
      "com.gu" % "content-entity-thrift" % contentEntityVersion
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
      "com.google.guava" % "guava" % guavaVersion % Test,
      "org.gnieh" %% "diffson-circe" % diffsonVersion % Test
    ),
    Compile / packageDoc / mappings := Nil
  )

lazy val benchmarks = Project(id = "benchmarks", base = file("benchmarks"))
  .dependsOn(json, scala)
  .settings(artifactProductionSettings)
  .enablePlugins(JmhPlugin)
  .settings(
    libraryDependencies += "com.google.guava" % "guava" % "19.0",
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
