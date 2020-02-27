package org.encryfoundation.tg.pipelines.chat

import canoe.api.{Scenario, _}
import canoe.models.outgoing.TextContent
import cats.Applicative
import cats.mtl.MonadState
import org.encryfoundation.tg.env.BotEnv
import org.encryfoundation.tg.pipelines.{EnvironmentPipe, Pipe, PipeEnv}

object PrintPipe {
  def apply[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](name: String): EnvironmentPipe[F] =
     EnvironmentPipe[F]((envPipe: PipeEnv) =>
      for {
        env <- Scenario.eval(MonadState[F, BotEnv[F]].get)
        _ <- Scenario.eval(envPipe.chat.get.send(TextContent(envPipe.variables(name)))(env.tgClient.get))
      } yield envPipe
    )
}
