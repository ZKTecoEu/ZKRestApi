import play.PlayScala

name := "ZKRestServer"

version := "0.11"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "com.nulab-inc" %% "play2-oauth2-provider" % "0.13.1",
  "com.typesafe.play" %% "play-slick" % "0.8.1",
  "com.wordnik" %% "swagger-play2" % "1.3.12"
)


fork in run := true