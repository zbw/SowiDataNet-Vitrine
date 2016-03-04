
name := """vitrine"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  "junit" % "junit" % "4.12" % "test",
  "org.webjars" % "bootstrap" % "3.3.6",
  "org.webjars" % "respond" % "1.4.2",
  "org.webjars" % "html5shiv" % "3.7.3"
)

includeFilter in (Assets, LessKeys.less) := "*.less"
excludeFilter in (Assets, LessKeys.less) := "_*.less"
lazy val root = (project in file(".")).enablePlugins(PlayJava)

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator


fork in run := false