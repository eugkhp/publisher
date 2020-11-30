package com.github.evgeniy.publisher.api

import caliban.{ GraphQL, RootResolver }
import cats.effect.Effect
import caliban.interop.cats.implicits._
import cats.implicits._
import com.github.evgeniy.publisher.services.{ Queue, Store, Subscribers }

object ApiSchema {

  type GQL = GraphQL[Any]

  def make[F[_]: Effect](db: Store[F], queue: Queue[F], subscribers: Subscribers[F]): F[GQL] = {
    case class HistoryArg(from: Int, to: Int)

    case class Queries(
      modules: HistoryArg => F[List[String]]
    )

    case class Mutation(
      publish: PublishArg => F[Boolean],
      subscribe: SubArg => F[Boolean],
      unsubscribe: UnSubArg => F[Boolean]
    )

    case class SubArg(addr: String)

    case class UnSubArg(addr: String)

    case class PublishArg(msg: String)

    val value = GraphQL
      .graphQL(
        RootResolver(
          Queries(arg => db.getMessages(arg.from, arg.to)),
          Mutation(
            arg => queue.pushMessage(arg.msg).as(true),
            arg => subscribers.subscribe(arg.addr).as(true),
            arg => subscribers.unsubscribe(arg.addr).as(true)
          )
        )
      )
    println(value.toString)
    Effect[F].pure(
      value
    )
  }
}
