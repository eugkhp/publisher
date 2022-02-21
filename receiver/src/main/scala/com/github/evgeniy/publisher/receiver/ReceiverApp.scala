package com.github.evgeniy.publisher.receiver

import caliban.Http4sAdapter
import cats.data.Kleisli
import org.http4s.StaticFile
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import tofu.logging.Logs
import zio._
import zio.interop.catz._
import zio.interop.catz.implicits._

object ReceiverApp extends App {
  type F[A] = Task[A]

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {
    val ec = platform.executor.asEC

    implicit val logs: Logs[F, F] = Logs.sync[F, F]

    Task.concurrentEffectWith { implicit CE =>
      Resources
        .make[F]
        .use { case Resources(config, schema, io) =>
          schema.interpreter.flatMap(in =>
            BlazeServerBuilder[F](ec)
              .bindHttp(config.httpPort, "localhost")
              .withHttpApp(
                Router[F](
                  "/api/graphql" -> CORS(Http4sAdapter.makeHttpService(in)),
                  "/ws/graphql"  -> CORS(Http4sAdapter.makeWebSocketService(in)),
                  "/graphiqlAPI" -> Kleisli.liftF(StaticFile.fromResource[F]("/graphiql.html", io, None))
                ).orNotFound
              )
              .serve
              .compile
              .drain
          )
        }
    }.exitCode
  }
}