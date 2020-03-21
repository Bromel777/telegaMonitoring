package org.encryfoundation.tg.pipelines.alerts

import canoe.api.Scenario
import canoe.api._
import retry._
import canoe.models.outgoing.TextContent
import cats.Monad
import cats.syntax.applicative._
import cats.effect.Timer
import cats.mtl.MonadState
import org.encryfoundation.tg.data.Errors.{BotError, PipeErr}
import org.encryfoundation.tg.env.BotEnv
import org.encryfoundation.tg.env.conditions.AlertCondition
import org.encryfoundation.tg.pipelines.{EnvironmentPipe, PipeEnv}
import retry.RetryPolicies
import tofu.Raise

import scala.concurrent.duration.FiniteDuration

object NotificationAlert {

  def apply[F[_]: MonadState[*[_], BotEnv[F]]: Monad](condition: AlertCondition[F])
                                                     (implicit f1: Raise[F, Throwable]): EnvironmentPipe[F] =
    EnvironmentPipe((pipeEnv: PipeEnv) =>
      for {
        conditionRes <- Scenario.eval(condition.check(pipeEnv))
        env <- Scenario.eval(MonadState[F, BotEnv[F]].get)
        _ <- if (conditionRes) Scenario.raiseError[F](PipeErr(pipeEnv.chat.get))
             else Scenario.done[F]
      } yield (pipeEnv)
  )
}
