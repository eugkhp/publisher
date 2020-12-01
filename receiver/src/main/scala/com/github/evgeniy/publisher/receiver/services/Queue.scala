package com.github.evgeniy.publisher.receiver.services

import cats.MonadError
import dev.profunktor.redis4cats.RedisCommands
import tofu.logging.{ Logging, Logs }
import cats.implicits._
import tofu.syntax.logging.LoggingInterpolator

trait Queue[F[_]] {
  def pushMessage(msg: String): F[Unit]
}

object Queue {
  def make[F[_]: MonadError[*[_], Throwable]](
    client: RedisCommands[F, String, String]
  )(implicit L: Logs[F, F]): F[Queue[F]] =
    for {
      implicit0(log: Logging[F]) <- Logs[F, F].forService[Queue.type]
    } yield new Impl[F](client)

  class Impl[F[_]: Logging: MonadError[*[_], Throwable]](client: RedisCommands[F, String, String]) extends Queue[F] {
    override def pushMessage(msg: String): F[Unit] =
      client.lPush("events", msg) *> info"Message added '$msg'"
  }
}
