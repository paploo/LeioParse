name := "LeioParse"

version := "0.1"

scalaVersion := "2.12.7"

scalacOptions in (Compile, compile) ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-target:jvm-1.8",
  "-encoding", "UTF-8",
  "-feature",
  "-Ywarn-unused",
  "-Ywarn-unused-import",
  "-Ywarn-infer-any"
)

libraryDependencies ++= Seq(
  "com.github.tototoshi" %% "scala-csv" % "1.3.5",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)

scalacOptions in (Compile,doc) := Seq("-groups", "-implicits")

autoAPIMappings := true