package com.github.evgeniy.publisher.services

trait Queue[F[_]] {
  def pushMessage(msg: String): F[Unit]
}

object Queue {

  def make[F[_]](): F[Queue[F]] = ???
}
