scalaVersion:="2.11.7"


lazy val notification = RootProject(file("../../Notification"))

val main = Project(id = "HealthCheck", base=file(".")).dependsOn(notification)


