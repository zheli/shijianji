name := "shijianji"

organization := "it.softfork"

version := "0.1"

scalaVersion := "2.12.8"

val akkaStreamVersion = "2.5.9"
val akkaHttpVersion = "10.0.9"
val slickVersion = "3.3.0"
libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.github.tototoshi" %% "scala-csv" % "1.3.5",
  "com.iheart" %% "ficus" % "1.4.5",
  "it.softfork" %% "debug4s" % "0.0.4",
  "com.micronautics" %% "web3j-scala" % "4.2.0" withSources(),
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test",
  "com.typesafe.akka" %% "akka-slf4j" % akkaStreamVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaStreamVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaStreamVersion % Test,
  "com.typesafe.play" %% "play-json" % "2.6.9",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
  "com.typesafe.slick" %% "slick" % slickVersion,
  "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
  "de.heikoseeberger" %% "akka-http-play-json" % "1.20.0",
  "org.scala-lang.modules" %% "scala-async" % "0.9.7",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "tech.minna" %% "play-json-macros" % "1.0.0",
)

resolvers ++= Seq(
  Resolver.bintrayRepo("liuhongchao", "maven"),
  Resolver.bintrayRepo("minna-technologies", "maven"),
  "micronautics/scala on bintray" at "http://dl.bintray.com/micronautics/scala",
  "Ethereum Maven" at "https://dl.bintray.com/ethereum/maven/"
)

//// https://github.com/irufus/gdax-java publish to maven local
//libraryDependencies ++= Seq(
//  "irufus" % "gdax-java" % "0.10.0"
//)
//resolvers += Resolver.mavenLocal

// Needed for play json macros macro annotations
addCompilerPlugin(
  "org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
