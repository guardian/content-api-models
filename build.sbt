name := "content-api-models"

organization := "com.gu"
libraryDependencies ++= Seq(
  "com.gu" % "story-packages-model-thrift" % "1.0.3",
  "com.gu" % "content-atom-model-thrift" % "1.0.0"
)
scalaVersion := "2.11.8"
publishMavenStyle := true
publishArtifact in Test := false
bintrayOrganization := Some("guardian")
bintrayRepository := "platforms"
licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))
publishArtifact in packageDoc := false
publishArtifact in packageSrc := false
unmanagedResourceDirectories in Compile += { baseDirectory.value / "src/main/thrift" }
crossPaths := false