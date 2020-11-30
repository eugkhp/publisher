package com.github.evgeniy.publisher

import caliban.Http4sAdapter
import cats.data.Kleisli
import org.http4s.StaticFile
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import zio._
import zio.interop.catz._
import zio.interop.catz.implicits._

object ReceiverApp extends App {
  type F[A] = Task[A]

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {
    val ec = platform.executor.asEC
    Task.concurrentEffectWith { implicit CE =>
      Resources
        .make[F]
        .use { case Resources(config, schema, io) =>
          schema.interpreter.flatMap(in =>
            BlazeServerBuilder[F](ec)
              .bindHttp(config.httpPort, "0.0.0.0")
              .withHttpApp(
                Router[F](
                  "/api/graphql" -> CORS(Http4sAdapter.makeHttpService(in)),
                  "/graphiql"    -> Kleisli.liftF(StaticFile.fromResource[F]("/graphiql.html", io, None))
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
