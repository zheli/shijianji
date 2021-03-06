name := "shijianji"

organization := "it.softfork"

version := "0.1"

scalaVersion := "2.12.10"

val akkaStreamVersion = "2.5.9"
val akkaHttpVersion = "10.1.11"
val slickVersion = "3.3.1"
libraryDependencies ++= Seq(
  "com.github.tototoshi" %% "scala-csv" % "1.3.5",
  "com.iheart" %% "ficus" % "1.4.5",
  "com.micronautics" %% "web3j-scala" % "4.2.0" withSources(),
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test",
  "com.typesafe.akka" %% "akka-slf4j" % akkaStreamVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaStreamVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaStreamVersion % Test,
  "com.typesafe.play" %% "play-json" % "2.6.9",
  "de.heikoseeberger" %% "akka-http-play-json" % "1.20.0",
  "org.scala-lang.modules" %% "scala-async" % "0.9.7",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "tech.minna" %% "play-json-macros" % "1.0.0",
  // crypto exchange APIs
  "org.knowm.xchange" % "xchange-core" % "4.4.1",
  "org.knowm.xchange" % "xchange-coinmarketcap" % "4.4.1",
  "org.knowm.xchange" % "xchange-examples" % "4.4.1",
  // logging
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  // database related
  "com.typesafe.slick" %% "slick" % slickVersion,
  "com.typesafe.slick" %% "slick-testkit" % slickVersion % "test",
  "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
  "com.github.tminglei" %% "slick-pg" % "0.18.1", // postgresql extensions
  "com.github.tminglei" %% "slick-pg_play-json" % "0.18.1", // play-json support
  "org.postgresql" % "postgresql" % "42.2.8", // postgres database driver
  "com.h2database" % "h2" % "1.4.199", // h2mem database driver
  // helpers
  "it.softfork" %% "debug4s" % "0.0.4"
)

// Fix the issue that logger stops working
libraryDependencies ~= { _.map(_.exclude("org.slf4j", "slf4j-nop")) }

resolvers ++= Seq(
  "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/",
  Resolver.bintrayRepo("liuhongchao", "maven"),
  Resolver.bintrayRepo("minna-technologies", "maven"),
  "micronautics/scala on bintray" at "https://dl.bintray.com/micronautics/scala",
  "Ethereum Maven" at "https://dl.bintray.com/ethereum/maven/"
)

// Needed for play json macros macro annotations
addCompilerPlugin(
  "org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

fork in run := true
