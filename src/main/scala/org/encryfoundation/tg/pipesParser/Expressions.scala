package org.encryfoundation.tg.pipesParser

import java.io

import fastparse._
import NoWhitespace._
import canoe.api.{Scenario, TelegramClient}
import canoe.models.Chat
import canoe.models.messages.TextMessage
import cats.Applicative
import cats.effect.IO
import cats.effect.concurrent.Ref
import cats.mtl.MonadState
import org.encryfoundation.tg.env.BotEnv
import org.encryfoundation.tg.pipelines.Pipe
import org.encryfoundation.tg.pipelines.chat.{InvokePipe, PrintPipe, ReadPipe}
import com.olegpy.meow.effects._

import scala.reflect.runtime.universe._

object Expressions {

  def combine[F[_]: MonadState[*[_], BotEnv[F]], I1, O1: TypeTag, I2: TypeTag, O2](p1: Pipe[F, I1, O1], p2: Pipe[F, I2, O2]): Pipe[F, I1, O2] = {
    val a = typeOf[O1]
    val b = typeOf[I2]
    println(s"A: $a, B: $b")
    if (b <:< a) p1.combine(p2.asInstanceOf[Pipe[F, O1, O2]])
    else throw new Exception("oops!")
  }

  def STRING[_: P] = P(CharIn("a-z").rep(1).!)

  def printPipe[F[_]: MonadState[*[_], BotEnv[F]]](implicit t: P[_]) =
    P("Print(" ~ STRING ~ ")").map(toPrint => PrintPipe(toPrint))

  def readPipe[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](implicit t: P[_]) =
    P("Read(" ~ STRING ~ ")").map(varName => ReadPipe[F](varName))

  def invokePipe[F[_]: MonadState[*[_], BotEnv[F]]](implicit t: P[_]) =
    P("Invoke(" ~ STRING ~ ")").map(invokeName => InvokePipe(invokeName))

  def apipes[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](implicit t: P[_]) =
    P( readPipe[F] | printPipe[F])

  def pipes[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](implicit t: P[_]) = P( " => " ~ apipes[F])

  def pipeline[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](implicit t: P[_]) =
    P (invokePipe ~ pipes.rep(1)).map { case (invokePipe, pipes) =>
      pipes.foldLeft(invokePipe.asInstanceOf[Pipe[F, Nothing, Any]]){
        case (pipeLine, nextPipe) =>
          combine[F, Nothing, Any, Nothing, Any](pipeLine, nextPipe.asInstanceOf[Pipe[F, Nothing, Any]])
      }
    }

  val pipe = "Invoke(testinvoke) => Read(variable) => Print(variable)"

  def command(tgClient: TelegramClient[IO]) = for {
    ref <- Ref[IO].of(BotEnv[IO](Some(tgClient), None))
    res <- ref.runState { implicit monadState =>
      Parser.parsePipes[IO](pipe)
    }
  } yield res.interpret

}
