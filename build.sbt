import sbtrelease.ReleaseStateTransformations._

name := "content-api-models"

organization := "com.gu"
libraryDependencies ++= Seq(
  "com.gu" % "story-packages-model-thrift" % "1.0.3",
  "com.gu" % "content-atom-model-thrift" % "1.0.0"
)
scalaVersion := "2.11.8"
crossPaths := false

unmanagedResourceDirectories in Compile += { baseDirectory.value / "src/main/thrift" }

publishMavenStyle := true
publishArtifact in Test := false
releasePublishArtifactsAction := PgpKeys.publishSigned.value
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

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))
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
  </developers>
)
pomIncludeRepository := { _ => false }
