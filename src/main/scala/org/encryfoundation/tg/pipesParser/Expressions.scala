package org.encryfoundation.tg.pipesParser

import canoe.api.TelegramClient
import cats.effect.IO
import cats.implicits._
import cats.mtl.MonadState
import cats.{Applicative, Monad}
import fastparse.NoWhitespace._
import fastparse._
import org.encryfoundation.tg.env.BotEnv
import org.encryfoundation.tg.pipelines.chat.{InvokePipe, PrintPipe, ReadPipe}
import org.encryfoundation.tg.pipelines.{Pipe, PipeEnv}
import tofu.Raise


object Expressions {

  def STRING[_: P] = P(CharIn("a-z").rep(1).!)

  def printPipe[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](implicit t: P[_]) =
    P("Print(" ~ STRING ~ ")").map(toPrint => PrintPipe(toPrint))

  def readPipe[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](implicit t: P[_]) =
    P("Read(" ~ STRING ~ ")").map(varName => ReadPipe[F](varName))

  def invokePipe[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](implicit t: P[_]) =
    P("Invoke(" ~ STRING ~ ")").map(invokeName => InvokePipe(invokeName))

  def apipes[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](implicit t: P[_]) =
    P( readPipe[F] | printPipe[F] )

  def pipes[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](implicit t: P[_]) = P( " => " ~ apipes[F])

  def pipeline[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](implicit t: P[_]) =
    P (invokePipe ~ pipes.rep(1)).map { case (invokePipe, pipes) =>
      pipes.foldLeft(invokePipe){
        case (pipeLine, nextPipe) =>
          pipeLine.combine(nextPipe)
      }
    }

  def parseCommand[F[_]: MonadState[*[_], BotEnv[F]]: Monad: Raise[*[_], Throwable]](tgClient: TelegramClient[IO])(pipeToParse: String) = for {
    res <- Parser.parsePipes[F](pipeToParse)
  } yield res.commonFunc(PipeEnv.empty.copy())
}
