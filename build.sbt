name := """charge-plan"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs
)

libraryDependencies ++= Seq(
  "org.mongodb" % "mongo-java-driver" % "2.13.0",
  "org.mongodb.morphia" % "morphia" % "0.110",
  "org.json" % "json" % "20140107",
  "com.google.guava" % "guava"  % "18.0",
  "com.google.http-client" % "google-http-client" % "1.19.0",
  "com.google.http-client" % "google-http-client-jackson2" % "1.19.0",
  "org.onebusaway" % "onebusaway-gtfs" % "1.3.3",
  "org.apache.commons" % "commons-collections4" % "4.0"
)