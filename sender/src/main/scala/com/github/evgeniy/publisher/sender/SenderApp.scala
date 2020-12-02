package com.github.evgeniy.publisher.sender

import alleycats.std.all.alleyCatsSetTraverse
import cats.implicits._
import fs2._
import tofu.logging.Logs
import zio.interop.catz._
import zio.interop.catz.implicits._
import zio._

import scala.concurrent.duration.DurationInt

object SenderApp extends App {
  type F[A] = Task[A]

  implicit val logs: Logs[F, F] = Logs.sync[F, F]

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] =
    Task.concurrentEffectWith { implicit CE =>
      Resources
        .make[F]
        .use { case Resources(queue, subs, sender) =>
          Stream
            .fixedRate[F](1 second)
            .evalMap(_ => queue.readMessage)
            .unNone
            .evalMap { msg =>
              subs.getSubs.flatMap { users =>
                users.parTraverse(u => sender.sendMessage(msg, u))
              }
            }
            .compile
            .drain
        }
    }.exitCode

}
