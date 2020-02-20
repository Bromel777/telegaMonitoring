package org.encryfoundation.tg.pipelines.chat

import canoe.api.Scenario
import canoe.api._
import canoe.syntax.{text, _}
import canoe.models.Chat
import org.encryfoundation.tg.pipelines.Pipe

case class ReadPipe[F[_], T](chat: Chat) extends Pipe[F, String]{
  override def interpret: Scenario[F, String] = Scenario.expect(text)
}
