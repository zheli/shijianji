name := "shijianji"

organization := "it.softfork"

version := "0.1"

scalaVersion := "2.12.6"

val akkaStreamVersion = "2.5.9"
val akkaHttpVersion = "10.0.9"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % akkaStreamVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaStreamVersion % Test,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test",
  "com.typesafe.play" %% "play-json" % "2.6.9",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "de.heikoseeberger" %% "akka-http-play-json" % "1.20.0",
  "tech.minna" %% "play-json-macros" % "1.0.0"
)

resolvers += Resolver.bintrayRepo("minna-technologies", "maven")

// Needed for play json macros macro annotations
addCompilerPlugin(
  "org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
