package org.encryfoundation.tg.pipesParser

import fastparse._
import NoWhitespace._
import canoe.api.TelegramClient
import canoe.models.Chat
import canoe.models.messages.TextMessage
import cats.mtl.MonadState
import org.encryfoundation.tg.env.BotEnv
import org.encryfoundation.tg.pipelines.Pipe
import org.encryfoundation.tg.pipelines.chat.{PrintPipe, ReadPipe}

object Expressions {

  def STRING[_: P] = P(CharIn("a-z").rep(1).!)

  def printPipe[_: P, F[_]: MonadState[*[_], BotEnv[F]]]: P[Pipe[F, String, TextMessage]] =
    P("Print(" ~ STRING ~ ")").map(toPrint => PrintPipe(toPrint))

  def readPipe[_: P, F[_]]: P[Pipe[F, Any, String]] =
    P("Read(" ~ STRING ~ ")").map(_ => ReadPipe())

  def expr[F[_]: MonadState[*[_], BotEnv[F]], _: P]: P[Pipe[F, Any, Any]] = readPipe
}
