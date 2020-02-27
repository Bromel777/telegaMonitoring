package org.encryfoundation.tg.pipelines.chat

import canoe.api.Scenario
import canoe.syntax.{text, _}
import cats.Applicative
import cats.mtl.MonadState
import cats.implicits._
import cats.syntax._
import org.encryfoundation.tg.env.BotEnv
import org.encryfoundation.tg.pipelines.{EnvironmentPipe, Pipe, PipeEnv}

object ReadPipe {
  def apply[F[_]: Applicative](varName: String)(implicit a: MonadState[F, BotEnv[F]]): EnvironmentPipe[F] =
     EnvironmentPipe((envPipe: PipeEnv) =>
      for {
        text <- Scenario.expect(text)
        _ <- Scenario.eval(println("put").pure[F])
      } yield envPipe.copy(variables = envPipe.variables + (varName -> text))
    )
}


