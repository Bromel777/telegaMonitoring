package org.encryfoundation.tg.pipelines.chat

import canoe.api.Scenario
import canoe.api._
import canoe.models.Chat
import canoe.models.messages.TextMessage
import canoe.models.outgoing.TextContent
import org.encryfoundation.tg.pipelines.Pipe

case class PrintPipe[F[_]: TelegramClient, T](toPrint: T, chat: Chat) extends Pipe[F, TextMessage] {
  override def interpret: Scenario[F, TextMessage] = Scenario.eval(chat.send(TextContent(toPrint.toString)))
}
