package org.encryfoundation.tg.pipelines.chat

import canoe.api.Scenario
import canoe.api._
import canoe.models.Chat
import canoe.models.messages.TextMessage
import canoe.models.outgoing.TextContent
import cats.mtl.MonadState
import fastparse.P
import cats.implicits._
import org.encryfoundation.tg.env.BotEnv
import org.encryfoundation.tg.pipelines.{Pipe, PipeCompanion}

final class PrintPipe[F[_], T] private (toPrint: T)
                                       (interpret: Scenario[F, TextMessage]) extends Pipe[F, T, TextMessage](interpret)

object PrintPipe extends PipeCompanion {

  def apply[F[_]: MonadState[*[_], BotEnv[F]], T](toPrint: T): Pipe[F, T, TextMessage] =
    new PrintPipe(toPrint)(
      for {
        env <- Scenario.eval(MonadState[F, BotEnv[F]].get)
        text <- Scenario.eval(env.chat.get.send(TextContent(toPrint.toString))(env.tgClient.get))
      } yield text
    )

  override val name: String = "PrintPipe"
}
