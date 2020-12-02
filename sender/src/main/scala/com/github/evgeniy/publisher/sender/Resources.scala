package com.github.evgeniy.publisher.sender

import cats.Parallel
import cats.effect.{ ConcurrentEffect, ContextShift, Effect, Resource, Sync, Timer }
import com.github.evgeniy.publisher.sender.service.{ Queue, Sender, Subscribers }
import com.github.evgeniy.publisher.syntax._
import com.typesafe.config.ConfigFactory
import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.connection.RedisClient
import dev.profunktor.redis4cats.data.RedisCodec
import dev.profunktor.redis4cats.effect.Log.Stdout._
import org.http4s.client.blaze.BlazeClientBuilder
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import tofu.logging.Logs

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

case class Resources[F[_]](
  queue: Queue[F],
  subscribers: Subscribers[F],
  sender: Sender[F]
)

object Resources {

  def make[F[_]: Effect: ConcurrentEffect: ContextShift: Parallel: Timer](implicit
    L: Logs[F, F]
  ): Resource[F, Resources[F]] =
    for {
      config      <- Sync[F].delay(ConfigFactory.load()).resource
      appConfig   <- Sync[F].delay(ConfigSource.fromConfig(config).loadOrThrow[AppConfig]).resource
      redisCli    <- RedisClient[F].from(appConfig.redis)
      redis       <- Redis[F].fromClient(redisCli, RedisCodec.Utf8)
      queue       <- Queue.make[F](redis).resource
      ec          <- Sync[F].delay(Executors.newFixedThreadPool(appConfig.threads)).resource.map(ExecutionContext.fromExecutor)
      subscribers <- Subscribers.make[F](redis).resource
      client      <- BlazeClientBuilder[F](ec).resource
      sender      <- Sender.make[F](client, subscribers).resource
    } yield Resources(queue, subscribers, sender)
}
