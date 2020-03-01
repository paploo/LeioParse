name := "LeioParse"

version := "0.1"

scalaVersion := "2.12.10"

val circeVersion = "0.10.0"

scalacOptions in (Compile, compile) ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-target:jvm-1.8",
  "-encoding", "UTF-8",
  "-feature",
  "-Ywarn-unused",
  "-Ywarn-unused-import",
  "-Ywarn-infer-any",
  "-Ypartial-unification" //Needed by cats
)

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "1.6.0",

  "com.github.tototoshi" %% "scala-csv" % "1.3.5",

  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,

  "com.github.scopt" %% "scopt" % "4.0.0-RC2",

  "com.lihaoyi" %% "pprint" % "0.5.3",

  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",

  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)

coverageEnabled in Test := true

scalacOptions in (Compile,doc) := Seq("-groups", "-implicits")

autoAPIMappings := true