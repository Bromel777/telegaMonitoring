package org.encryfoundation.tg.pipelines.chat

import canoe.api.Scenario
import canoe.api._
import canoe.models.Chat
import canoe.models.messages.TextMessage
import canoe.models.outgoing.TextContent
import org.encryfoundation.tg.pipelines.Pipe

final class PrintPipe[F[_]: TelegramClient, T] private (toPrint: T, chat: Chat)
                                                       (interpret: Scenario[F, TextMessage]) extends Pipe[F, T, TextMessage](interpret)

object PrintPipe {

  def apply[F[_]: TelegramClient, T](toPrint: T, chat: Chat): PrintPipe[F, T] =
    new PrintPipe(toPrint, chat)(Scenario.eval(chat.send(TextContent(toPrint.toString))))
}
