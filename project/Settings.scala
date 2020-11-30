import sbt.CrossVersion
import sbt.Keys.{ scalaVersion, scalacOptions }
import sbt._

object Versions {
  val scala       = "2.13.3"
  val fs2         = "2.4.5"
  val tofu        = "0.8.0"
  val derevo      = "0.11.4"
  val sttp        = "3.0.0-RC7"
  val http4s      = "0.21.13"
  val cats        = "2.2.0"
  val cats_effect = "2.2.0"
  val circe       = "0.14.0-M1"
  val monix       = "3.3.0"
  val caliban     = "0.9.3"
  val logback     = "1.2.3"
  val pureConfig  = "0.14.0"
}

object Settings {

  lazy val compilerOptions = Seq(
    "-Ymacro-annotations",
    "-Ypatmat-exhaust-depth",
    "40"
  )
  lazy val commonSettings = Seq(
    scalaVersion := Versions.scala,
    scalacOptions ++= Settings.compilerOptions
  )
  lazy val compilerPlugins = Seq(
    "org.typelevel" %% "kind-projector"     % "0.11.1" cross CrossVersion.full,
    "com.olegpy"    %% "better-monadic-for" % "0.3.1"
  )

  lazy val cats = Seq(
    "org.typelevel" %% "cats-core"   % Versions.cats,
    "org.typelevel" %% "cats-effect" % Versions.cats_effect
  )

  lazy val logging = Seq(
    "ch.qos.logback" % "logback-classic" % Versions.logback,
    "ru.tinkoff"    %% "tofu-logging"    % Versions.tofu
  )

  lazy val tofu = Seq(
    "ru.tinkoff" %% "tofu",
    "ru.tinkoff" %% "tofu-config",
    "ru.tinkoff" %% "tofu-derivation"
  ).map(_ % Versions.tofu)

  lazy val fs2 = Seq(
    "co.fs2" %% "fs2-core",
    "co.fs2" %% "fs2-io"
  ).map(_ % Versions.fs2)

  lazy val monix = Seq("io.monix" %% "monix" % Versions.monix)

  lazy val sttp = Seq(
    "com.softwaremill.sttp.client3" %% "core",
    "com.softwaremill.sttp.client3" %% "circe"
  ).map(_ % Versions.sttp)

  lazy val graphql = Seq(
    "com.github.ghostdogpr" %% "caliban",
    "com.github.ghostdogpr" %% "caliban-http4s",
    "com.github.ghostdogpr" %% "caliban-cats"
  ).map(_ % Versions.caliban)

  lazy val derevo = Seq(
    "org.manatki" %% "derevo-core",
    "org.manatki" %% "derevo-cats",
    "org.manatki" %% "derevo-circe"
  ).map(_ % Versions.derevo)

  lazy val circe = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser",
    "io.circe" %% "circe-shapes"
  ).map(_ % Versions.circe)

  lazy val http4s = Seq(
    "org.http4s" %% "http4s-dsl",
    "org.http4s" %% "http4s-blaze-server",
    "org.http4s" %% "http4s-blaze-client",
    "org.http4s" %% "http4s-circe"
  ).map(_ % Versions.http4s)

  lazy val pureConfig = Seq(
    "com.github.pureconfig" %% "pureconfig",
    "com.github.pureconfig" %% "pureconfig-cats"
  ).map(_ % Versions.pureConfig)

  lazy val redis4cats = Seq(
    "dev.profunktor" %% "redis4cats-effects",
    "dev.profunktor" %% "redis4cats-log4cats"
  ).map(_ % "0.10.3")

  lazy val receiverDeps = graphql
  lazy val senderDeps   = sttp

  lazy val commonDeps = derevo ++ logging ++ cats ++ tofu ++ http4s ++ circe ++ fs2 ++ pureConfig ++ monix ++ redis4cats

}
