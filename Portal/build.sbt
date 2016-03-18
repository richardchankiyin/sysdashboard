name := """Portal"""

version := "1.0-SNAPSHOT"

lazy val healthcheck = RootProject(file("../HealthCheck"))

lazy val root = (project in file(".")).dependsOn(healthcheck).enablePlugins(PlayScala)



scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test
)


libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.6"

libraryDependencies += "org.scalamock" % "scalamock-scalatest-support_2.11" % "3.2.2"

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
