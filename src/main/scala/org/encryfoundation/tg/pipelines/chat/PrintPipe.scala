package org.encryfoundation.tg.pipelines.chat

import canoe.api.{Scenario, _}
import canoe.models.messages.TextMessage
import canoe.models.outgoing.TextContent
import cats.mtl.MonadState
import org.encryfoundation.tg.env.BotEnv
import org.encryfoundation.tg.pipelines.Pipe

final class PrintPipe[F[_]] private (interpret: Scenario[F, TextMessage]) extends Pipe[F, Unit, TextMessage](interpret)

object PrintPipe {

  def apply[F[_]: MonadState[*[_], BotEnv[F]]](name: String): Pipe[F, Unit, TextMessage] =
    new PrintPipe(
      for {
        env <- Scenario.eval(MonadState[F, BotEnv[F]].get)
        text <- Scenario.eval(env.chat.get.send(TextContent(env.vars(name)))(env.tgClient.get))
      } yield text
    )
}
