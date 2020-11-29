package com.github.evgeniy.publisher

import cats.data.{ EitherT, OptionT }
import cats.effect.{ Concurrent, Resource }
import cats.implicits._
import cats.{ Applicative, Functor }

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

  implicit class FireForget[F[_], A](val fa: F[A]) extends AnyVal {
    def fork(implicit F: Concurrent[F]): F[Unit]                    = F.start(fa).void
    def forkAndReturn[B](other: B)(implicit F: Concurrent[F]): F[B] = F.start(fa).as(other)
  }
}
