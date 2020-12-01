package com.github.evgeniy.publisher.receiver.services

import cats.MonadError
import dev.profunktor.redis4cats.RedisCommands
import tofu.logging.{ Logging, Logs }
import cats.implicits._
import tofu.syntax.logging.LoggingInterpolator

trait Store[F[_]] {
  def getMessages(from: Int, to: Int): F[List[String]]

  def saveMessage(msg: String): F[Unit]
}

object Store {
  def make[F[_]: MonadError[*[_], Throwable]](
    client: RedisCommands[F, String, String]
  )(implicit L: Logs[F, F]): F[Store[F]] =
    for {
      implicit0(log: Logging[F]) <- Logs[F, F].forService[Store.type]
    } yield new Impl[F](client)

  class Impl[F[_]: Logging: MonadError[*[_], Throwable]](client: RedisCommands[F, String, String]) extends Store[F] {
    override def getMessages(from: Int, to: Int): F[List[String]] =
      client.lRange("history", from.toLong, to.toLong)

    override def saveMessage(msg: String): F[Unit] =
      client.lPush("history", msg) *> info"Message '$msg' saved"
  }
}
