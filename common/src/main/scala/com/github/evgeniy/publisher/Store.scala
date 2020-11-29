package com.github.evgeniy.publisher

import java.net.URI

trait Store[F[_]] {
  def addMessage(msg: String): F[Unit]

  def getMessages(from: Int, to: Int): F[List[String]]

}

object Store {
  def make[F[_]](uri: URI): F[Store[F]] = ???
}
