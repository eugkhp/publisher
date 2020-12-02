package com.github.evgeniy.publisher.sender.service

import cats.MonadError
import cats.effect.Timer
import cats.implicits._
import com.github.evgeniy.publisher.syntax._
import com.github.evgeniy.publisher.{ Retry, RetryPolicy }
import derevo.circe.encoder
import derevo.derive
import io.circe.syntax._
import org.http4s.Method.POST
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.headers.Accept
import org.http4s.{ MediaType, Uri }
import tofu.logging.{ Logging, Logs }
import tofu.syntax.logging.LoggingInterpolator

import scala.concurrent.duration.DurationInt

trait Sender[F[_]] {
  def sendMessage(msg: String, addr: String): F[Unit]
}

object Sender {

  @derive(encoder)
  case class Payload(payload: String)

  def make[F[_]: MonadError[*[_], Throwable]: Timer](
    client: Client[F],
    subs: Subscribers[F]
  )(implicit L: Logs[F, F]): F[Sender[F]] =
    for {
      implicit0(log: Logging[F]) <- Logs[F, F].forService[Sender.type]
    } yield new Impl(client, subs)

  class Impl[F[_]: MonadError[*[_], Throwable]: Logging: Timer](cl: Client[F], subs: Subscribers[F])
      extends Sender[F]
      with Http4sClientDsl[F] {

    private val traceF: Retry.TraceF[F, Unit] = {
      case (Left(e), _) => Logging[F].warnCause(s"Retrying", e)
      case _            => Logging[F].warn(s"Retrying")
    }

    private val retryPolicy = RetryPolicy
      .simple[F, Unit](maxAttempt = 3, 1.seconds)
      .copy(trace = traceF.some)

    private def sendKickMessage(addr: String) =
      (for {
        _   <- info"Sending kick msg to $addr"
        req <- POST(Payload("You are kicked").asJson, Uri.unsafeFromString(addr), Accept(MediaType.application.json))
        _   <- cl.successful(req)
      } yield ()).handleErrorWith(_ => info"Sub $addr is ignored kick message")

    override def sendMessage(msg: String, addr: String): F[Unit] =
      (for {
        _   <- info"Sending message '$msg' to $addr"
        req <- POST(Payload(msg).asJson, Uri.unsafeFromString(addr), Accept(MediaType.application.json))
        _   <- cl.successful(req)
      } yield ())
        .retry(retryPolicy)
        .handleErrorWith(_ =>
          for {
            _ <- info"Sub $addr didn't receive the message"
            _ <- subs.removeSub(addr)
            _ <- sendKickMessage(addr)
          } yield ()
        )
  }

}
