import sbt.Keys._
import sbtrelease.ReleaseStateTransformations._

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
    </developers>
  ),
  publishTo := sonatypePublishToBundle.value,
  publishConfiguration := publishConfiguration.value.withOverwrite(true),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false }
)

val commonSettings = Seq(
  scalaVersion := "2.13.1",
  crossScalaVersions := Seq("2.11.12", "2.12.10", scalaVersion.value),
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  organization := "com.gu",
  licenses := Seq("Apache v2" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")),
  resolvers += Resolver.sonatypeRepo("public")
) ++ mavenSettings

def customDeps(scalaVersion: String) = {
  val (circeVersion, diffsonVersion, fezziwigVersion) = CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, 11)) => ("0.11.0", "3.1.1", "1.2")
    case _ => ("0.12.0", "4.0.0", "1.3")
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

/**
  * Root project
  */
lazy val root = Project(id = "root", base = file("."))
  .settings(commonSettings)
  .aggregate(models, json, scala)
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
      releaseStepCommandAndRemaining("+publishSigned"),
      releaseStepCommand("sonatypeBundleRelease"),
      setNextVersion,
      commitNextVersion,
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
    crossPaths := false,
    publishArtifact in packageDoc := false,
    publishArtifact in packageSrc := false,
    includeFilter in unmanagedResources := "*.thrift",
    unmanagedResourceDirectories in Compile += { baseDirectory.value / "src/main/thrift" }
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
    scroogeThriftOutputFolder in Compile := sourceManaged.value / "thrift",
    scroogeThriftSourceFolder in Compile := baseDirectory.value / "../models/src/main/thrift",
    scroogeThriftDependencies in Compile ++= Seq(
      "story-packages-model-thrift",
      "content-atom-model-thrift",
      "content-entity-thrift"
    ),
    // See: https://github.com/twitter/scrooge/issues/199
    scroogeThriftSources in Compile ++= {
      (scroogeUnpackDeps in Compile).value.flatMap { dir => (dir ** "*.thrift").get }
    },
    scroogePublishThrift in Compile := false,
    libraryDependencies ++= Seq(
      "org.apache.thrift" % "libthrift" % "0.12.0",
      "com.twitter" %% "scrooge-core" % "19.9.0",
      "com.gu" % "story-packages-model-thrift" % "2.0.2",
      "com.gu" % "content-atom-model-thrift" % "3.1.0",
      "com.gu" % "content-entity-thrift" % "2.0.2"
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
