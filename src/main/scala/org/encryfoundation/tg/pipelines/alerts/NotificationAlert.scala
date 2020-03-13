package org.encryfoundation.tg.pipelines.alerts

import canoe.api.Scenario
import canoe.api._
import cats.syntax.applicative._
import canoe.models.outgoing.TextContent
import cats.Applicative
import cats.mtl.MonadState
import org.encryfoundation.tg.env.BotEnv
import org.encryfoundation.tg.env.conditions.AlertCondition
import org.encryfoundation.tg.pipelines.{EnvironmentPipe, PipeEnv}

object NotificationAlert {

  def apply[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](condition: AlertCondition[F]): EnvironmentPipe[F] = EnvironmentPipe((pipeEnv: PipeEnv) =>
    for {
      conditionRes <- Scenario.eval(condition.check(pipeEnv))
      env <- Scenario.eval(MonadState[F, BotEnv[F]].get)
      _ <- if (conditionRes) Scenario.eval(pipeEnv.chat.get.send(TextContent("Alert!"))(env.tgClient.get))
           else Scenario.done[F]
    } yield (pipeEnv)
  )
}
