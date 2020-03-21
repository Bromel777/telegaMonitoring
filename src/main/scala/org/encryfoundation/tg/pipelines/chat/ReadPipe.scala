package org.encryfoundation.tg.pipelines.chat

import canoe.api.Scenario
import canoe.syntax.{text, _}
import cats.Applicative
import cats.mtl.MonadState
import org.encryfoundation.tg.env.BotEnv
import org.encryfoundation.tg.pipelines.json.{StringJsonType, Value}
import org.encryfoundation.tg.pipelines.{EnvironmentPipe, Pipe, PipeEnv}

object ReadPipe {
  def apply[F[_]: Applicative](varName: String)(implicit a: MonadState[F, BotEnv[F]]): EnvironmentPipe[F] =
     EnvironmentPipe((envPipe: PipeEnv) =>
      for {
        text <- Scenario.expect(text)
      } yield envPipe.copy(variables = envPipe.variables + (varName -> Value(varName, text, StringJsonType)))
    )
}


