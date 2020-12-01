package com.github.evgeniy.publisher.sender

import cats.effect.Sync
import tofu.logging.Logs
import cats.implicits._
import fs2._
import zio._
import zio.interop.catz._
import zio.interop.catz.implicits._

import scala.concurrent.duration.DurationInt

object SenderApp extends App {
  type F[A] = Task[A]

  implicit val logs: Logs[F, F] = Logs.sync[F, F]

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] =
    Task.concurrentEffectWith { implicit CE =>
      Resources
        .make[F]
        .use { case Resources(queue, _) =>
          Stream
            .fixedRate[F](1 second)
            .evalMap(_ => queue.readMessage)
            .unNone
            .evalMap(x => Sync[F] delay (println(x)))
            .compile
            .drain

        }
    }.exitCode

}
