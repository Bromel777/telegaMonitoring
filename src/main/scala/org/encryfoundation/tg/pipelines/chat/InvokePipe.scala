package org.encryfoundation.tg.pipelines.chat

import canoe.api.{Scenario, TelegramClient, _}
import canoe.models.Chat
import canoe.syntax._
import org.encryfoundation.tg.pipelines.Pipe

case class InvokePipe[F[_]: TelegramClient](name: String) extends Pipe[F, Chat]{
  override def interpret: Scenario[F, Chat] = Scenario.expect(command(name).chat)
}
