package com.github.evgeniy.publisher

import cats.effect.{ Blocker, ConcurrentEffect, ContextShift, Effect, Resource, Sync }
import com.github.evgeniy.publisher.api.ApiSchema
import com.github.evgeniy.publisher.api.ApiSchema.GQL
import com.github.evgeniy.publisher.services.{ Queue, Store, Subscribers }
import com.typesafe.config.ConfigFactory
import pureconfig.ConfigSource
import com.github.evgeniy.publisher.syntax._
import pureconfig.generic.auto._
import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.effect.Log.Stdout._
import tofu.logging.Logs

case class Resources[F[_]](
  appConfig: AppConfig,
  schema: GQL,
  io: Blocker
)

object Resources {

  def make[F[_]: Effect: ConcurrentEffect: ContextShift](implicit L: Logs[F, F]): Resource[F, Resources[F]] =
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
