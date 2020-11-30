package com.github.evgeniy.publisher.services

trait Subscribers[F[_]] {
  def subscribe(addr: String): F[Unit]
  def unsubscribe(addr: String): F[Unit]
}

object Subscribers {

  def make[F[_]](): F[Subscribers[F]] = ???
}
