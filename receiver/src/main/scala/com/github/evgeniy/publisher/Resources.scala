package com.github.evgeniy.publisher

import cats.effect.{ Blocker, Effect, Resource, Sync }
import com.github.evgeniy.publisher.api.ApiSchema
import com.github.evgeniy.publisher.api.ApiSchema.GQL
import com.github.evgeniy.publisher.services.{ Queue, Subscribers }
import com.typesafe.config.ConfigFactory
import pureconfig.ConfigSource
import com.github.evgeniy.publisher.syntax._
import pureconfig.generic.auto._

case class Resources[F[_]](
  appConfig: AppConfig,
  schema: GQL,
  io: Blocker
)

object Resources {
  def make[F[_]: Effect]: Resource[F, Resources[F]] =
    for {
      config      <- Sync[F].delay(ConfigFactory.load()).resource
      appConfig   <- Sync[F].delay(ConfigSource.fromConfig(config).loadOrThrow[AppConfig]).resource
      db          <- Store.make[F](appConfig.queueUri).resource
      queue       <- Queue.make[F]().resource
      subscribers <- Subscribers.make[F]().resource
      schema      <- ApiSchema.make[F](db, queue, subscribers).resource
      io          <- Blocker.apply[F]
    } yield Resources[F](appConfig, schema, io)
}
