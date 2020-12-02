package com.github.evgeniy.publisher.sender.service

import cats.MonadError
import cats.implicits._
import dev.profunktor.redis4cats.RedisCommands
import tofu.logging.{ Logging, Logs }
import tofu.syntax.logging.LoggingInterpolator

trait Subscribers[F[_]] {
  def getSubs: F[Set[String]]

  def removeSub(sub: String): F[Unit]
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
    override def getSubs: F[Set[String]] = for {
      subs <- client.sMembers("subscribers")
      _    <- info"Subscribers received '${subs.mkString(";")}'"
    } yield subs

    override def removeSub(sub: String): F[Unit] =
      client.sRem("subscribers", sub) *> info"Subscriber kicked '$sub'"
  }

}
