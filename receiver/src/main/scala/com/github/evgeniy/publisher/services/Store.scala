package com.github.evgeniy.publisher.services

import cats.MonadError
import dev.profunktor.redis4cats.RedisCommands
import tofu.logging.{ Logging, Logs }
import cats.implicits._

trait Store[F[_]] {
  def getMessages(from: Int, to: Int): F[List[String]]
}

object Store {
  def make[F[_]: MonadError[*[_], Throwable]](
    client: RedisCommands[F, String, String]
  )(implicit L: Logs[F, F]): F[Store[F]] =
    for {
      implicit0(log: Logging[F]) <- Logs[F, F].forService[Store.type]
    } yield (new Impl[F](client))

  class Impl[F[_]](client: RedisCommands[F, String, String]) extends Store[F] {
    override def getMessages(from: Int, to: Int): F[List[String]] =
      client.lRange("history", from.toLong, to.toLong)
  }
}
