package com.github.evgeniy.publisher

import caliban.Http4sAdapter
import cats.Parallel
import cats.data.Kleisli
import cats.effect.{ Blocker, Clock, ConcurrentEffect, ContextShift, ExitCode, Fiber, Resource, Timer, _ }
import com.github.evgeniy.publisher.api.ApiSchema
import com.typesafe.config.ConfigFactory
import tofu.logging.{ Logging, Logs }
import pureconfig.generic.auto._
import com.github.evgeniy.publisher.syntax._
import org.http4s.StaticFile
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import pureconfig.ConfigSource
import zio._
import zio.interop.catz._
import zio.interop.catz.implicits._

import scala.concurrent.ExecutionContext

class AppF[F[_]: Timer: ContextShift: Clock: Parallel](implicit F: ConcurrentEffect[F]) {

  implicit val logs: Logs[F, F] = Logs.sync[F, F]

  case class Resources(
    appConfig: AppConfig,
    db: Store[F],
    io: Blocker
  )

  def resources: Resource[F, Resources] =
    for {
      config                     <- F.delay(ConfigFactory.load()).resource
      implicit0(log: Logging[F]) <- Logs[F, F].byName("receiver").resource
      appConfig                  <- F.delay(ConfigSource.fromConfig(config).loadOrThrow[AppConfig]).resource
      db                         <- Store.make[F](appConfig.queueUri).resource
      io                         <- Blocker.apply[F]
    } yield Resources(appConfig, db, io)

  def launch(r: Resources): F[Unit] =
    for {
//      implicit0(log: Logging[F]) <- Logs[F, F].byName("receiver")
      interpreter <- new ApiSchema[F](r.db).schema.interpreter
      _ <- BlazeServerBuilder[F](ExecutionContext.global)
             .bindHttp(8088, "localhost")
             .withHttpApp(
               Router(
                 "/api/graphql" -> CORS(Http4sAdapter.makeHttpService(interpreter)),
                 "/graphiql"    -> Kleisli.liftF(StaticFile.fromResource[F]("/graphiql.html", r.io, None))
               ).orNotFound
             )
    } yield ()

  def run: F[Unit] = resources.use(r => launch(r))
}

object ReceiverApp extends App {

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {}
}
