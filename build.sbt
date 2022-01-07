import sbt.Keys._
import sbtrelease.ReleaseStateTransformations._
import sbtrelease.{Version, versionFormatError}

val contentEntityVersion = "2.0.6"
val contentAtomVersion = "3.2.4"
val storyPackageVersion = "2.0.4"
val thriftVersion = "0.15.0"

val candidateReleaseType = "candidate"
val candidateReleaseSuffix = "-RC1"
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
    case v if v == candidateReleaseType => candidateReleaseSuffix
    case v if v == snapshotReleaseType => snapshotReleaseSuffix
  }.map { suffix =>
    releaseVersion := {
      ver => Version(ver).map(_.withoutQualifier.string).map(_.concat(suffix)).getOrElse(versionFormatError(ver))
    }
  }.toSeq
}

lazy val commonSettings = Seq(
  scalaVersion := "2.13.2",
  crossScalaVersions := Seq("2.11.12", "2.12.11", scalaVersion.value),
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  organization := "com.gu",
  licenses := Seq("Apache v2" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")),
  resolvers += Resolver.sonatypeRepo("public")
) ++ mavenSettings ++ versionSettingsMaybe

def customDeps(scalaVersion: String) = {
  val (circeVersion, diffsonVersion, fezziwigVersion) = CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, 11)) => ("0.11.0", "3.1.1", "1.2")
    case _ => ("0.12.0", "4.0.0", "1.4")
  }
  Seq(
    "com.gu" %% "fezziwig" % fezziwigVersion,
    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion,
    "io.circe" %% "circe-optics" % circeVersion,
    "org.gnieh" %% "diffson-circe" % diffsonVersion % "test"
  )
}

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
    case v if v == candidateReleaseType => candidateReleaseType.toUpperCase
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
  val commonSteps = Seq(
    checkReleaseType,
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest
  )

  val prodSteps: Seq[ReleaseStep] = Seq(
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommandAndRemaining("+publishSigned"),
    releaseStepCommand("sonatypeBundleRelease"),
    setNextVersion,
    commitNextVersion,
    pushChanges
  )

  /*
  SNAPSHOT versions are published directly to Sonatype snapshot repo and no local bundle is assembled
  Also, we cannot use `sonatypeBundleUpload` or `sonatypeRelease` commands which are usually wrapped up
  within a call to `sonatypeBundleRelease` (https://github.com/xerial/sbt-sonatype#publishing-your-artifact).

  Therefore SNAPSHOT versions are not promoted to Maven Central and clients will have to ensure they have the
  appropriate resolver entry in their build.sbt, e.g.

  resolvers += Resolver.sonatypeRepo("snapshots")

  */
  val snapshotSteps: Seq[ReleaseStep] = Seq(
    setReleaseVersion,
    releaseStepCommandAndRemaining("+publishSigned"),
    setNextVersion
  )

  /*
  Release Candidate assemblies can be published to Sonatype and Maven.

  To make this work, start SBT with the candidate releaseType;
    sbt -DRELEASE_TYPE=candidate

  This gets around the "problem" of sbt-sonatype assuming that a -SNAPSHOT build should not be delivered to Maven.

  In this mode, the version number will be presented as e.g. 1.2.3-RC1, but the git tagging and version-updating
  steps are not triggered, so it's up to the developer to keep track of what was released and manipulate subsequent
  release and next versions appropriately.
  */
  val candidateSteps: Seq[ReleaseStep] = Seq(
    setReleaseVersion,
    releaseStepCommandAndRemaining("+publishSigned"),
    releaseStepCommand("sonatypeBundleRelease"),
    setNextVersion
  )

  // remember to set with sbt -DRELEASE_TYPE=snapshot|candidate if running a non-prod release
  commonSteps ++ (sys.props.get("RELEASE_TYPE") match {
    case Some(v) if v == snapshotReleaseType => snapshotSteps // this deploys -SNAPSHOT build to sonatype snapshot repo only
    case Some(v) if v == candidateReleaseType => candidateSteps // this enables a release candidate build to sonatype and Maven
    case None => prodSteps  // our normal deploy route
  })

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
      "com.twitter" %% "scrooge-core" % "20.4.1",
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
      "org.scalatest" %% "scalatest" % "3.0.8" % "test",
      "com.google.guava" % "guava" % "19.0" % "test"
    ) ++ customDeps(scalaVersion.value),
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

lazy val typescript = (project in file("ts"))
  .enablePlugins(ScroogeTypescriptGen)
  .settings(commonSettings)
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
      "com.twitter" %% "scrooge-core" % "20.4.1",
      "com.gu" % "story-packages-model-thrift" % storyPackageVersion,
      "com.gu" % "content-atom-model-thrift" % contentAtomVersion,
      "com.gu" % "content-entity-thrift" % contentEntityVersion
    )
  )