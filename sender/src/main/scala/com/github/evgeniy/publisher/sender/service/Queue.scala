package com.github.evgeniy.publisher.sender.service

import cats.MonadError
import cats.implicits._
import dev.profunktor.redis4cats.RedisCommands
import tofu.logging.{ Logging, Logs }
import tofu.syntax.logging.LoggingInterpolator

trait Queue[F[_]] {
  def readMessage: F[Option[String]]
}

object Queue {
  def make[F[_]: MonadError[*[_], Throwable]](
    client: RedisCommands[F, String, String]
  )(implicit L: Logs[F, F]): F[Queue[F]] =
    for {
      implicit0(log: Logging[F]) <- Logs[F, F].forService[Queue.type]
    } yield new Impl[F](client)

  class Impl[F[_]: Logging: MonadError[*[_], Throwable]](client: RedisCommands[F, String, String]) extends Queue[F] {
    override def readMessage: F[Option[String]] =
      for {
        msg <- client.rPop("events")
        _   <- info"Message read '$msg'"
      } yield msg
  }
}
