package org.encryfoundation.tg.pipelines.chat

import canoe.api.{Scenario, TelegramClient, _}
import canoe.models.Chat
import canoe.syntax._
import org.encryfoundation.tg.pipelines.Pipe

final class InvokePipe[F[_]: TelegramClient] private (name: String)
                                                     (interpret: Scenario[F, Chat]) extends Pipe[F, Any, Chat](interpret)

object InvokePipe {

  def apply[F[_]: TelegramClient](name: String): InvokePipe[F] =
    new InvokePipe(name)(Scenario.expect(command(name).chat))
}
