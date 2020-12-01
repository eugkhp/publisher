package com.github.evgeniy.publisher.sender

import cats.Parallel
import cats.effect.{ ConcurrentEffect, ContextShift, Effect, Resource, Sync }
import com.github.evgeniy.publisher.sender.service.{ Queue, Subscribers }
import com.typesafe.config.ConfigFactory
import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.connection.RedisClient
import dev.profunktor.redis4cats.data.RedisCodec
import dev.profunktor.redis4cats.effect.Log.Stdout._
//import dev.profunktor.redis4cats.pubsub.PubSub
import pureconfig.ConfigSource
import tofu.logging.Logs
import com.github.evgeniy.publisher.syntax._
import pureconfig.generic.auto._

case class Resources[F[_]](
  queue: Queue[F],
  subscribers: Subscribers[F]
)

object Resources {

  def make[F[_]: Effect: ConcurrentEffect: ContextShift: Parallel](implicit L: Logs[F, F]): Resource[F, Resources[F]] =
    for {
      config     <- Sync[F].delay(ConfigFactory.load()).resource
      appConfig  <- Sync[F].delay(ConfigSource.fromConfig(config).loadOrThrow[AppConfig]).resource
      redisCli   <- RedisClient[F].from(appConfig.redis)
      redis      <- Redis[F].fromClient(redisCli, RedisCodec.Utf8)
      queue      <- Queue.make[F](redis).resource
      subscriber <- Subscribers.make[F](redis).resource

    } yield Resources(queue, subscriber)
}
