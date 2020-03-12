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
import org.encryfoundation.tg.pipelines.json.HttpApiJsonParsePipe
import org.encryfoundation.tg.pipelines.{Pipe, PipeEnv}
import tofu.Raise

object Expressions {

  def STRING[_: P] = P(CharIn("a-z").rep(1).!)
  def subDirUrl[_: P] = P("/" ~ CharIn("a-z").rep(1).!)
  def URL[_: P] = P(
    "http://" ~ CharIn("a-z").rep(1).! ~ "." ~ CharIn("a-z").rep(1).! ~ subDirUrl.rep(0).!
  ).map { case (el1, el2, el3) => el1 + el2 + el3}

  def printPipe[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](implicit t: P[_]) =
    P("Print(" ~ STRING ~ ")").map(toPrint => PrintPipe(toPrint))

  def readPipe[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](implicit t: P[_]) =
    P("Read(" ~ STRING ~ ")").map(varName => ReadPipe[F](varName))

  def invokePipe[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](implicit t: P[_]) =
    P("Invoke(" ~ STRING ~ ")").map(invokeName => InvokePipe(invokeName))

  def httpApiJsonParsePipe[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](implicit t: P[_], f1: Raise[F, Throwable]) =
    P("ParseJson(" ~ URL ~ ")").map { case urlToParse => HttpApiJsonParsePipe(url = urlToParse)}

  def apipes[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](implicit t: P[_], f1: Raise[F, Throwable]) =
    P( httpApiJsonParsePipe[F] | readPipe[F] | printPipe[F] )

  def pipes[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](implicit t: P[_], f1: Raise[F, Throwable]) = P(" => " ~ apipes[F])

  def pipeline[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](implicit t: P[_], f1: Raise[F, Throwable]) =
    P (invokePipe ~ pipes.rep(1)).map { case (invokePipe, pipes) =>
      pipes.foldLeft(invokePipe){
        case (pipeLine, nextPipe) =>
          pipeLine.combine(nextPipe)
      }
    }

  def parseCommand[F[_]: MonadState[*[_], BotEnv[F]]: Monad](tgClient: TelegramClient[IO])(pipeToParse: String)(implicit f1: Raise[F, Throwable]) = for {
    res <- Parser.parsePipes[F](pipeToParse)
  } yield res.commonFunc(PipeEnv.empty.copy())
}
