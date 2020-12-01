package com.github.evgeniy.publisher

import cats.{ Applicative, MonadError }
import cats.effect.Timer
import cats.implicits._
import com.github.evgeniy.publisher.Retry.TraceF

import scala.concurrent.duration.FiniteDuration
import scala.util.control.NoStackTrace

case object RetryException extends Throwable with NoStackTrace

object Retry {
  type TraceF[F[_], A] = (Either[Throwable, A], Int) => F[Unit]

  def retry[F[_]: Timer, A](
    fa: => F[A],
    policy: RetryPolicy[F, A]
  )(implicit F: MonadError[F, Throwable]): F[A] = {
    val empty: (Either[Throwable, A], Int) => F[Unit] = (_, _) => Applicative[F].unit

    retryImp(
      fa,
      policy.breakCondition,
      policy.maxAttempt,
      policy.initDelay,
      policy.nextDelay,
      policy.trace.getOrElse[TraceF[F, A]](empty)
    )
  }

  private def retryImp[F[_]: Timer, A](
    fa: => F[A],
    breakCondition: Either[Throwable, A] => Boolean,
    attempt: Int,
    initDelay: FiniteDuration,
    nextDelay: FiniteDuration => FiniteDuration,
    trace: TraceF[F, A]
  )(implicit F: MonadError[F, Throwable]): F[A] =
    if (attempt <= 0)
      F.raiseError(RetryException)
    else {
      fa.attempt.flatMap(a =>
        if (breakCondition(a)) F.fromEither(a)
        else {
          Timer[F].sleep(initDelay) >> trace(a, attempt - 1) >> retryImp(
            fa,
            breakCondition,
            attempt - 1,
            nextDelay(initDelay),
            nextDelay,
            trace
          )
        }
      )
    }
}

case class RetryPolicy[F[_], A](
  breakCondition: Either[Throwable, A] => Boolean,
  maxAttempt: Int,
  initDelay: FiniteDuration,
  nextDelay: FiniteDuration => FiniteDuration,
  trace: Option[TraceF[F, A]]
)

object RetryPolicy {
  def simple[F[_], A](maxAttempt: Int, initDelay: FiniteDuration): RetryPolicy[F, A] =
    RetryPolicy[F, A](bc => bc.fold(_ => false, _ => true), maxAttempt, initDelay, nd => nd * 2, None)
}
