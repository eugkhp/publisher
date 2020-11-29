package com.github.evgeniy.publisher

import cats.Parallel
import cats.effect.{ Clock, ConcurrentEffect, ContextShift, ExitCode, Timer }
import monix.eval.{ Task, TaskApp }
import tofu.logging.Logs

class AppF[F[_]: Timer: ContextShift: Clock: Parallel](implicit F: ConcurrentEffect[F]) {
  implicit val logs: Logs[F, F] = Logs.sync[F, F]

  case class Resources(
    config: AppConfig
  )

}

class SenderApp extends TaskApp {
  override def run(args: List[String]): Task[ExitCode] = ???
}
