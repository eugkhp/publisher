//package com.github.evgeniy.publisher.api
//
//import cats.effect.{ Blocker, ConcurrentEffect, ContextShift, Sync, Timer }
//import cats.implicits._
//import org.http4s.client.middleware
//import org.http4s.server.Router
//import org.http4s.server.middleware.Logger
//import tofu.logging.Logging
//
//import scala.concurrent.duration._
//import org.http4s.server.middleware.CORS
//import org.http4s.StaticFile
//import cats.data.Kleisli
//import org.http4s.implicits._
//import org.http4s.server.blaze._
//import caliban.Http4sAdapter
//
//object HttpEndpoint {
//
//  def launch[F[_]: Sync: ConcurrentEffect: ContextShift: Timer: Logging](
//    graphqlApi: ApiSchema[F],
//    io: Blocker,
//    httpPort: Int
//  ): F[Unit] = {
//
//    def logReq: String => F[Unit] = msg => Logging[F].debug(msg)
//
////    val loggedRoutesV1 =
////      Logger.httpRoutes(logHeaders = false, logBody = true, logAction = Some(logReq))(api.apiRoutesV1)
//
//    val app = Router(
//      "/api/graphql" -> CORS(Http4sAdapter.makeHttpService(graphqlApi.schema.interpreter)),
//      "/graphiql"    -> Kleisli.liftF(StaticFile.fromResource("/graphiql.html", io, None))
//    ).orNotFound
//
//    BlazeServerBuilder[F]
//      .bindHttp(httpPort, "0.0.0.0")
//      .withHttpApp(app)
//      .withoutBanner
//      .withResponseHeaderTimeout(60.seconds)
//      .serve
//      .compile
//      .drain
//  }
//}
