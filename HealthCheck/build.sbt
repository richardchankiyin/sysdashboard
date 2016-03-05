name:="HealthCheck"

version:="1.0.0"

scalaVersion:="2.11.7"

lazy val notification = RootProject(file("../Notification"))

val main = Project(id = "HealthCheck", base=file(".")).dependsOn(notification)

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"
libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.13"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2"
libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.6"
libraryDependencies += "com.typesafe" % "config" % "1.3.0"
libraryDependencies += "it.sauronsoftware.cron4j" % "cron4j" % "2.2.5"


