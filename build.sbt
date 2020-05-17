import Dependencies._

ThisBuild / scalaVersion     := "2.13.2"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "net.atos"
ThisBuild / organizationName := "atos"

lazy val root = (project in file("."))
  .settings(
    name := "Atos Recruitment Task",
    libraryDependencies ++= Seq(
      scalaTest % Test,
      "org.json4s" %% "json4s-native" % "3.7.0-M4"
    ),
    scalacOptions ++= Seq("-deprecation", "-feature")
  )
