import Settings._

inThisBuild(
  scalaVersion := Versions.scala
)

lazy val publisher = project
  .in(file("."))
  .settings(commonSettings)
  .aggregate(receiver, sender)

lazy val receiver = project
  .in(file("receiver"))
  .enablePlugins(JavaAppPackaging)
  .dependsOn(common)
  .settings(commonSettings)
  .settings(
    Settings.compilerPlugins.map(addCompilerPlugin): _*
  )
  .settings(libraryDependencies ++= receiverDeps)

lazy val sender = project
  .in(file("sender"))
  .enablePlugins(JavaAppPackaging)
  .dependsOn(common)
  .settings(commonSettings)
  .settings(
    Settings.compilerPlugins.map(addCompilerPlugin): _*
  )
  .settings(libraryDependencies ++= receiverDeps)

lazy val common = project
  .in(file("common"))
  .settings(commonSettings)
  .settings(libraryDependencies ++= commonDeps)
  .settings(
    Settings.compilerPlugins.map(addCompilerPlugin): _*
  )
