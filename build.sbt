ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "Aws_Grpc"
  )

Compile / PB.targets := Seq(
  scalapb.gen() -> (Compile / sourceManaged).value / "scalapb"
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

val awsjavaVersion = "1.12.90"
val typesafeConfigVersion = "1.4.1"
val awsjavas3Version = "1.12.98"
val logbackVersion = "1.3.0-alpha10"
val apacheCommonIOVersion = "2.11.0"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % typesafeConfigVersion,
  "com.amazonaws" % "aws-java-sdk-s3" % awsjavas3Version,
  "com.amazonaws" % "aws-lambda-java-events" % "3.10.0",
  "com.amazonaws" % "aws-lambda-java-core" % "1.2.1",
  "org.scalaj" %% "scalaj-http" % "2.4.2",
  "ch.qos.logback" % "logback-core" % logbackVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "com.thesamet.scalapb" %% "scalapb-json4s" % "0.12.0",
  "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
  "io.grpc" % "grpc-examples" % "0.7.2",
  "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
  "org.scalatest"%%"scalatest"%"3.2.14"%Test,
  "org.scalatest"%%"scalatest-featurespec"%"3.2.14"%Test,
  "commons-io" % "commons-io" % apacheCommonIOVersion,
)