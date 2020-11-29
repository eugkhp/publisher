package com.github.evgeniy.publisher.api

import caliban.{ GraphQL, RootResolver }
import cats.effect.Effect
import com.github.evgeniy.publisher.Store
import caliban.interop.cats.implicits._

class ApiSchema[F[_]: Effect](db: Store[F]) {

  case class HistoryArg(from: Int, to: Int)

  case class Queries(
    modules: HistoryArg => F[List[String]]
  )

  case class Mutation(
    subscribe: SubArg => F[Boolean],
    unsubscribe: UnSubArg => F[Boolean],
    publish: PublishArg => F[Boolean]
  )

  val schema = GraphQL.graphQL(RootResolver(Queries(arg => db.getMessages(arg.from, arg.to))))

  case class SubArg(addr: String)

  case class UnSubArg(addr: String)

  case class PublishArg(msg: String)

}
