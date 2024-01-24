import sbt.Keys.*
import sbt.{Test, Tests}
import sbtrelease.ReleaseStateTransformations.*
import sbtrelease.{Version, versionFormatError}

// dependency versions
val contentEntityVersion = "2.2.1"
val contentAtomVersion = "4.0.0"
val storyPackageVersion = "2.2.0"
val thriftVersion = "0.15.0"
val scroogeVersion = "22.1.0" // update plugins too if this version changes
val circeVersion = "0.14.1"
val fezziwigVersion = "1.6"

// dependency versions (for tests only)
val scalaTestVersion = "3.0.8"
val guavaVersion = "19.0"
val diffsonVersion = "4.1.1"

// support non-production release types
val betaReleaseType = "beta"
val betaReleaseSuffix = "-beta.0"
val snapshotReleaseType = "snapshot"
val snapshotReleaseSuffix = "-SNAPSHOT"

lazy val mavenSettings = Seq(
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
      <developer>
        <id>regiskuckaertz</id>
        <name>Regis Kuckaertz</name>
        <url>https://github.com/regiskuckaertz</url>
      </developer>
      <developer>
        <id>annebyrne</id>
        <name>Anne Byrne</name>
        <url>https://github.com/annebyrne</url>
      </developer>
       <developer>
        <id>justinpinner</id>
        <name>Justin Pinner</name>
        <url>https://github.com/justinpinner</url>
      </developer>
    </developers>
  ),
  publishTo := sonatypePublishToBundle.value,
  publishConfiguration := publishConfiguration.value.withOverwrite(true),
  publishMavenStyle := true,
  Test / publishArtifact := false,
  pomIncludeRepository := { _ => false }
)

lazy val versionSettingsMaybe = {
  sys.props.get("RELEASE_TYPE").map {
    case v if v == betaReleaseType => betaReleaseSuffix
    case v if v == snapshotReleaseType => snapshotReleaseSuffix
  }.map { suffix =>
    releaseVersion := {
      ver => Version(ver).map(_.withoutQualifier.string).map(_.concat(suffix)).getOrElse(versionFormatError(ver))
    }
  }.toSeq
}

lazy val commonSettings = Seq(
  scalaVersion := "2.13.12",
  // downgrade scrooge reserved word clashes to warnings
  Compile / scroogeDisableStrict := true,
  // scrooge 21.3.0: Builds are now only supported for Scala 2.12+
  // https://twitter.github.io/scrooge/changelog.html#id11
  crossScalaVersions := Seq("2.12.18", scalaVersion.value),
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  organization := "com.gu",
  licenses := Seq("Apache v2" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")),
  resolvers ++= Resolver.sonatypeOssRepos("public"),
  Test / testOptions +=
    Tests.Argument(TestFrameworks.ScalaTest, "-u", s"test-results/scala-${scalaVersion.value}", "-o")
) ++ mavenSettings ++ versionSettingsMaybe

/*
 Trialling being able to release snapshot versions from WIP branch without updating back to git
 e.g. $ sbt [-DRELEASE_TYPE=snapshot|candidate] release cross
 or
      $ sbt [-DRELEASE_TYPE=snapshot|candidate]
      sbt> release cross
      sbt> project typeScript
      sbt> releaseNpm <version>

 One downside here is that you'd have to (I think) exit and re-start sbt without the -D when you
 want to run a non-snapshot release. This is probably fine because we only run releases from
 our main/master branch when changes are merged in normal circumstances.
*/

lazy val checkReleaseType: ReleaseStep = ReleaseStep({ st: State =>
  val releaseType = sys.props.get("RELEASE_TYPE").map {
    case v if v == betaReleaseType => betaReleaseType.toUpperCase
    case v if v == snapshotReleaseType => snapshotReleaseType.toUpperCase
  }.getOrElse("PRODUCTION")

  SimpleReader.readLine(s"This will be a $releaseType release. Continue? [y/N]: ") match {
    case Some(v) if Seq("Y", "YES").contains(v.toUpperCase) => // we don't care about the value - it's a flow control mechanism
    case _ => sys.error(s"Release aborted by user!")
  }
  // we haven't changed state, just pass it on if we haven't thrown an error from above
  st
})

lazy val releaseProcessSteps: Seq[ReleaseStep] = {
  val commonSteps:Seq[ReleaseStep] = Seq(
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
  )

  val localExtraSteps:Seq[ReleaseStep] = Seq(
    commitReleaseVersion,
    tagRelease,
    publishArtifacts,
    setNextVersion,
    commitNextVersion
  )

  val snapshotSteps:Seq[ReleaseStep] = Seq(
    publishArtifacts,
    releaseStepCommand("sonatypeReleaseAll")
  )

  val prodSteps:Seq[ReleaseStep] = Seq(
    releaseStepCommandAndRemaining("+publishSigned"),
    releaseStepCommand("sonatypeBundleRelease")
  )

  val localPostRelease:Seq[ReleaseStep]  = Seq(
    pushChanges,
  )

  (sys.props.get("RELEASE_TYPE"), sys.env.get("CI")) match {
    case (Some(v), None) if v == snapshotReleaseType => commonSteps ++ localExtraSteps ++ snapshotSteps ++ localPostRelease
    case (_, None) => commonSteps ++ localExtraSteps ++ prodSteps ++ localPostRelease
    case (Some(v), _) if v == snapshotReleaseType => commonSteps ++ snapshotSteps
    case (_, _)=> commonSteps ++ prodSteps
  }
}

/**
  * Root project
  */
lazy val root = Project(id = "root", base = file("."))
  .settings(commonSettings)
  .aggregate(models, json, scala)
  .settings(
    publishArtifact := false,
    releaseProcess := releaseProcessSteps
  )

/**
  * Thrift models project
  */
lazy val models = Project(id = "content-api-models", base = file("models"))
  .settings(commonSettings)
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
  .settings(commonSettings)
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
  .dependsOn(scala % "provided")
  .settings(commonSettings)
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
  .settings(commonSettings)
  .enablePlugins(JmhPlugin)
  .settings(
    libraryDependencies += "com.google.guava" % "guava" % "19.0",
    Jmh / javaOptions ++= Seq("-server", "-Xms4G", "-Xmx4G", "-XX:+UseG1GC", "-XX:-UseBiasedLocking"),
    publishArtifact := false
  )

lazy val npmBetaReleaseTagMaybe =
  sys.props.get("RELEASE_TYPE").map {
    case v if v == betaReleaseType =>
      // Why hard-code "beta" instead of using the value of the variable? That's to ensure it's always presented as
      // --tag beta to the npm release process provided by the ScroogeTypescriptGen plugin regardless of how we identify
      // a beta release here
      scroogeTypescriptPublishTag := "beta"

    case v if v == snapshotReleaseType =>
      scroogeTypescriptPublishTag := "snapshot"
  }.toSeq

lazy val typescript = (project in file("ts"))
  .enablePlugins(ScroogeTypescriptGen)
  .settings(commonSettings)
  .settings(npmBetaReleaseTagMaybe)
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
