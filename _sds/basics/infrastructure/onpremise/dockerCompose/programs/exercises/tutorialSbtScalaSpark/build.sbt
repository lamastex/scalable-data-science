scalaVersion := "2.11.12"
organization := "org.lamastex"

scalacOptions ++= Seq("-deprecation")

resolvers += Resolver.sonatypeRepo("releases")

// this will allow managed dependecies and have us change versions easily
// but then we have more copies of jars for sbt cached
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.3.1",
  "org.apache.spark" %% "spark-sql" % "2.3.1",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "junit" % "junit" % "4.13-beta-1"
)

