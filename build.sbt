import java.nio.file.{Files, Paths}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys

import scala.io.Source.fromFile

val specs2Version: String = "3.8.4"
val playVersion: String = "2.5.8"
val enumeratumVersion = "1.4.13"

val testDependencies = Seq(
  "com.github.tomakehurst" % "wiremock" % "2.1.11" jar(),
  "com.typesafe.play" %% "play-test" % playVersion,
  "com.typesafe.play" %% "play-specs2" % playVersion,
  "org.specs2" %% "specs2-core" % specs2Version,
  "org.specs2" %% "specs2-junit" % specs2Version,
  "org.specs2" %% "specs2-mock" % specs2Version
).map(_.exclude("log4j", "log4j")).map(_.exclude("commons-logging", "commons-logging")).map(_ % "test")

val appDependencies: Seq[sbt.ModuleID] = Seq(
  ws,
  cache,
  filters,
  "com.softwaremill.macwire" %% "macros" % "2.2.0",
  "com.beachape" %% "enumeratum" % enumeratumVersion,
  "com.beachape" %% "enumeratum-play" % enumeratumVersion,
  "org.asynchttpclient" % "async-http-client" % "2.0.2",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.13",
  "com.typesafe.akka" %% "akka-actor" % "2.4.9",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.9"
).map(_.exclude("log4j", "log4j")).map(_.exclude("commons-logging", "commons-logging"))

val javaVersion = "1.8"
val encoding = "utf-8"
val scalaVersionString = "2.11.8"

val timeStampNow = LocalDateTime.now()
val dTformatter = DateTimeFormatter.ofPattern("uuuuMMddHHmmss")

val prodScalacOptions = Seq(
  "-feature",
  "-language:postfixOps",
  "-target:jvm-" + javaVersion,
  "-unchecked",
  "-deprecation",
  "-encoding", encoding,
  "-Ywarn-dead-code",
  "-Ywarn-infer-any")

val notPackagedFiles = Set("application-logger.xml", "additional-application.conf")

lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")

lazy val root = Project(id = "root", base = file(".")).settings(
  name := "carjump-challenge",
  version := timeStampNow.format(dTformatter),
  scalaVersion := scalaVersionString,
  scalaVersion in ThisBuild := scalaVersionString,
  libraryDependencies ++= appDependencies ++ testDependencies,
  javacOptions ++= Seq("-source", javaVersion, "-target", javaVersion, "-Xlint"),
  scalacOptions ++= prodScalacOptions,
  scalacOptions in Test ~= { (options: Seq[String]) =>
    options filterNot (option => option.startsWith("-X") || Seq("-Ywarn-dead-code", "-Ywarn-infer-any").contains(option))
  },
  javaOptions := Seq("-Dlogger.file=conf/application-logger.xml"),
  testOptions in Test += Tests.Argument("-Dlogback.statusListenerClass=ch.qos.logback.core.status.NopStatusListener"),
  scalastyleFailOnError := true,
  compileScalastyle := org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Compile).toTask("").value,
  (compile in Compile) := {(compile in Compile) dependsOn compileScalastyle}.value,

  routesGenerator := play.routes.compiler.InjectedRoutesGenerator,

  cancelable in Global := true,

  /**
    * scalariform
    */
  ScalariformKeys.preferences := ScalariformKeys.preferences.value
    .setPreference(RewriteArrowSymbols, false)
    .setPreference(AlignArguments, true)
    .setPreference(DoubleIndentClassDeclaration, true)
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(SpacesAroundMultiImports, true)
    .setPreference(AlignParameters, true)
    .setPreference(RewriteArrowSymbols, true),

  excludeFilter in scalariformFormat := (excludeFilter in scalariformFormat).value ||
    "Routes.scala" ||
    "ReverseRoutes.scala" ||
    "JavaScriptReverseRoutes.scala" ||
    "RoutesPrefix.scala"
).enablePlugins(PlayScala)
