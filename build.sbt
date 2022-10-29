ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "Aws_Grpc"
  )

val awsjavaVersion = "1.12.90"
val typesafeConfigVersion = "1.4.1"
val awsjavas3Version = "1.12.98"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % typesafeConfigVersion,
  "com.amazonaws" % "aws-java-sdk-s3" % awsjavas3Version
)