package org.encryfoundation.tg.pipelines.chat

import canoe.api.Scenario
import canoe.api._
import canoe.syntax.{text, _}
import canoe.models.Chat
import org.encryfoundation.tg.pipelines.Pipe

final class ReadPipe[F[_]] private (chat: Chat)(interpret: Scenario[F, String]) extends Pipe[F, Any, String](interpret)

object ReadPipe {
  def apply[F[_], T](chat: Chat): ReadPipe[F] = new ReadPipe(chat)(Scenario.expect(text))
}


