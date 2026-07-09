val flywayPlayVersion = "10.1.0"

val scalaVersion_2_13 = "2.13.18"
val scalaVersion_3 = "3.3.8"

val defaultFlywayVersion = "11.8.2"
val flywayVersion = sys.env.getOrElse("FLYWAY_PLAY_FLYWAY_VERSION", defaultFlywayVersion)

val scalikejdbcVersion = "4.3.5"

ThisBuild / versionScheme := Some("early-semver")
ThisBuild / version := flywayPlayVersion

val scalatest = "org.scalatest" %% "scalatest" % "3.2.20" % "test"

lazy val commonSettings = Seq(
  organization := "com.ponkotuy",
  scalaVersion := scalaVersion_2_13,
  crossScalaVersions := Seq(scalaVersion_2_13, scalaVersion_3),
  sonatypeCredentialHost := xerial.sbt.Sonatype.sonatypeCentralHost,
  publishTo := sonatypePublishToBundle.value,
  credentials ++= (for {
    username <- sys.env.get("SONATYPE_USERNAME")
    password <- sys.env.get("SONATYPE_PASSWORD")
  } yield Credentials("Sonatype Nexus Repository Manager", "central.sonatype.com", username, password)).toSeq
)

lazy val `flyway-play` = project
  .in(file("."))
  .settings(commonSettings)
  .settings(nonPublishingSettings)
  .aggregate(plugin, playapp)

lazy val plugin = project
  .in(file("plugin"))
  .enablePlugins(SbtTwirl)
  .settings(commonSettings)
  .settings(publishingSettings)
  .settings(
    name := "flyway-play",
    version := flywayPlayVersion,
    libraryDependencies ++= Seq(
      "org.playframework" %% "play" % play.core.PlayVersion.current % "provided",
      "org.playframework" %% "play-test" % play.core.PlayVersion.current % "test"
        excludeAll ExclusionRule(organization = "org.specs2"),
      "org.flywaydb" % "flyway-core" % flywayVersion,
      scalatest
    ),
    scalacOptions ++= Seq("-language:_", "-deprecation")
  )

val playAppName = "playapp"
val playAppVersion = "1.0-SNAPSHOT"

lazy val playapp = project
  .in(file("playapp"))
  .enablePlugins(PlayScala)
  .settings(commonSettings)
  .settings(nonPublishingSettings)
  .settings(
    Test / resourceDirectories += baseDirectory.value / "conf",
    version := playAppVersion,
    libraryDependencies ++= Seq(
      guice,
      "com.h2database" % "h2" % "2.4.240",
      "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.17.3",
      "org.playframework" %% "play-test" % play.core.PlayVersion.current % "test"
        excludeAll ExclusionRule(organization = "org.specs2"),
      "org.scalikejdbc" %% "scalikejdbc" % scalikejdbcVersion % "test",
      "org.scalikejdbc" %% "scalikejdbc-config" % scalikejdbcVersion % "test",
      scalatest
    )
  )
  .dependsOn(plugin)
  .aggregate(plugin)

val publishingSettings = Seq(
  publishMavenStyle := true,
  Test / publishArtifact := false,
  description := "Flyway module for Play Framework (fork of playframework/flyway-play)",
  pomExtra :=
    <url>https://github.com/ponkotuy/flyway-play</url>
      <licenses>
        <license>
          <name>Apache License, Version 2.0</name>
          <url>https://github.com/ponkotuy/flyway-play/blob/main/LICENSE.txt</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:ponkotuy/flyway-play.git</url>
        <connection>scm:git:git@github.com:ponkotuy/flyway-play.git</connection>
      </scm>
      <developers>
        <developer>
          <id>tototoshi</id>
          <name>Toshiyuki Takahashi</name>
          <url>https://tototoshi.github.io</url>
        </developer>
        <developer>
          <id>ponkotuy</id>
          <name>ponkotuy</name>
          <url>https://github.com/ponkotuy</url>
        </developer>
      </developers>
)

val nonPublishingSettings = Seq(
  publish / skip := true,
  publishArtifact := false,
  publish := {},
  publishLocal := {},
  Test / parallelExecution := false
)
