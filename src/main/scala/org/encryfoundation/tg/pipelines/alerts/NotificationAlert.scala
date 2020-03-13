package org.encryfoundation.tg.pipelines.alerts

import canoe.api.Scenario
import canoe.api._
import canoe.models.outgoing.TextContent
import cats.Applicative
import cats.mtl.MonadState
import org.encryfoundation.tg.env.{AlertCondition, BotEnv}
import org.encryfoundation.tg.pipelines.{EnvironmentPipe, PipeEnv}

object NotificationAlert {

  def apply[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](condition: AlertCondition): EnvironmentPipe[F] = EnvironmentPipe((pipeEnv: PipeEnv) =>
    for {
      conditionRes <- Scenario.eval(condition.check[F](pipeEnv))
      env <- Scenario.eval(MonadState[F, BotEnv[F]].get)
      _ <- if (conditionRes) Scenario.eval(pipeEnv.chat.get.send(TextContent("Alert!"))(env.tgClient.get))
           else Scenario.done[F]
    } yield (pipeEnv)
  )
}
