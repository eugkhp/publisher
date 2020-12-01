package com.github.evgeniy.publisher

import cats.data.{ EitherT, OptionT }
import cats.effect.{ Resource, Timer }
import cats.{ Applicative, Functor, MonadError }

object syntax {
  implicit class ResourceListOption[F[_], A](val fa: F[A]) extends AnyVal {
    def resource(implicit F: Applicative[F]): Resource[F, A] = Resource.liftF(fa)
  }

  implicit class OptionTOps[F[_], A](val fa: F[A]) extends AnyVal {
    def option(implicit F: Functor[F]): OptionT[F, A] = OptionT.liftF(fa)
  }

  implicit class EitherTOps[F[_], A](val fa: F[A]) extends AnyVal {
    def either[E](implicit F: Functor[F]): EitherT[F, E, A] = EitherT.liftF[F, E, A](fa)
  }

  implicit final class retryOps[F[_]: MonadError[*[_], Throwable]: Timer, A](fa: F[A]) {
    def retry(policy: RetryPolicy[F, A]): F[A] =
      Retry.retry[F, A](fa, policy)
  }
}
