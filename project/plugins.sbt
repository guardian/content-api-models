addSbtPlugin("com.github.sbt" % "sbt-release" % "1.5.0")
addSbtPlugin("ch.epfl.scala" % "sbt-version-policy" % "3.3.0")
addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.3.5")

addSbtPlugin("com.gu" % "scrooge-sbt-plugin" % "1.0.0-PREVIEW.get-release-workflow-working.2026-11-24T1045.c6dd9a1f")
addSbtPlugin("com.gu" % "sbt-scrooge-typescript" % "4.0.2")

addDependencyTreePlugin

// Patch the build tool graph without changing published library dependencies.
dependencyOverrides += "org.codehaus.plexus" % "plexus-utils" % "3.6.1"
