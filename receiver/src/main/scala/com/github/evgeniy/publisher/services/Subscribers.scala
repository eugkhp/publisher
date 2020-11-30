package com.github.evgeniy.publisher.services

import cats.MonadError
import dev.profunktor.redis4cats.RedisCommands
import tofu.logging.{ Logging, Logs }
import cats.implicits._
import tofu.syntax.logging.LoggingInterpolator

trait Subscribers[F[_]] {
  def subscribe(addr: String): F[Unit]
  def unsubscribe(addr: String): F[Unit]
}

object Subscribers {
  def make[F[_]: MonadError[*[_], Throwable]](
    client: RedisCommands[F, String, String]
  )(implicit L: Logs[F, F]): F[Subscribers[F]] =
    for {
      implicit0(log: Logging[F]) <- Logs[F, F].forService[Subscribers.type]
    } yield new Impl[F](client)

  class Impl[F[_]: Logging: MonadError[*[_], Throwable]](client: RedisCommands[F, String, String])
      extends Subscribers[F] {
    override def subscribe(addr: String): F[Unit] =
      client.sAdd("subscribers", addr) *> info"Subscriber added '$addr'"

    override def unsubscribe(addr: String): F[Unit] =
      client.sRem("subscribers", addr) *> info"Subscriber removed '$addr'"
  }
}
