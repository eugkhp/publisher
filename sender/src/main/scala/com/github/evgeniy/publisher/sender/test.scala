//package com.github.evgeniy.publisher.sender
//
//import cats.effect.{ ExitCode, IO, IOApp }
//import dev.profunktor.redis4cats.connection.RedisClient
//import dev.profunktor.redis4cats.data._
//import dev.profunktor.redis4cats.pubsub.PubSub
//import fs2.{ Pipe, Stream }
//import dev.profunktor.redis4cats.effect.Log.Stdout._
//import scala.concurrent.duration._
//import scala.util.Random
//
//object PubSubDemo extends IOApp {
//
//  private val stringCodec = RedisCodec.Utf8
//
//  private val eventsChannel = RedisChannel("events")
//  private val gamesChannel  = RedisChannel("games")
//
//  def sink(name: String): Pipe[IO, String, Unit] = _.evalMap(x => IO(println(s"Subscriber: $name >> $x")))
//
//  val program: Stream[IO, Unit] = {
//
//    for {
//      client <- Stream.resource(RedisClient[IO].from("redis://localhost"))
//      pubSub <- PubSub.mkPubSubConnection[IO, String, String](client, stringCodec)
//      sub1    = pubSub.subscribe(eventsChannel)
//      sub2    = pubSub.subscribe(gamesChannel)
//      pub1    = pubSub.publish(eventsChannel)
//      pub2    = pubSub.publish(gamesChannel)
//      rs <- Stream(
//              sub1.through(sink("#events")),
//              sub2.through(sink("#games")),
//              Stream.awakeEvery[IO](3.seconds) >> Stream
//                .eval(IO {
//                  val r = Random.nextInt(100).toString
//                  println(s"Next $r")
//                  r
//                })
//                .through(pub1),
//              Stream.awakeEvery[IO](5.seconds) >> Stream.emit("Pac-Man!").through(pub2),
//              Stream.awakeDelay[IO](11.seconds) >> pubSub.unsubscribe(gamesChannel),
//              Stream.awakeEvery[IO](6.seconds) >> pubSub
//                .pubSubSubscriptions(List(eventsChannel, gamesChannel))
//                .evalMap(x => IO(println(x)))
//            ).parJoin(6).drain
//    } yield rs
//  }
//  override def run(args: List[String]): IO[ExitCode] =
//    program.compile.drain.as(ExitCode.Success)
//
//}
