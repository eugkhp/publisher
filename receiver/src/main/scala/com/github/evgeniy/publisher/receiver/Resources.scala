package com.github.evgeniy.publisher.receiver

import cats.Parallel
import cats.effect.{ Blocker, ConcurrentEffect, ContextShift, Effect, Resource, Sync }
import com.github.evgeniy.publisher.receiver.api.ApiSchema
import com.github.evgeniy.publisher.receiver.api.ApiSchema.GQL
import com.github.evgeniy.publisher.receiver.services.{ Queue, Store, Subscribers }
import com.github.evgeniy.publisher.syntax._
import com.typesafe.config.ConfigFactory
import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.effect.Log.Stdout._
import pureconfig.ConfigSource
import tofu.logging.Logs
import pureconfig.generic.auto._

case class Resources[F[_]](
  appConfig: AppConfig,
  schema: GQL,
  io: Blocker
)

object Resources {

  def make[F[_]: Effect: ConcurrentEffect: ContextShift: Parallel](implicit L: Logs[F, F]): Resource[F, Resources[F]] =
    for {
      config      <- Sync[F].delay(ConfigFactory.load()).resource
      appConfig   <- Sync[F].delay(ConfigSource.fromConfig(config).loadOrThrow[AppConfig]).resource
      redis       <- Redis[F].utf8(appConfig.redis)
      db          <- Store.make[F](redis).resource
      queue       <- Queue.make[F](redis).resource
      subscribers <- Subscribers.make[F](redis).resource
      schema      <- ApiSchema.make[F](db, queue, subscribers).resource
      io          <- Blocker.apply[F]
    } yield Resources[F](appConfig, schema, io)
}
