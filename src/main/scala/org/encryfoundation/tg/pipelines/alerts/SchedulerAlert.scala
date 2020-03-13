package org.encryfoundation.tg.pipelines.alerts

import canoe.api.Scenario
import cats.Applicative
import cats.mtl.MonadState
import cats.syntax.applicative._
import cats.syntax.parallel._
import cats.instances.list._
import cats.kernel.Monoid
import org.encryfoundation.tg.env.BotEnv
import org.encryfoundation.tg.env.conditions.AlertCondition
import org.encryfoundation.tg.pipelines.{EnvironmentPipe, Pipe, PipeEnv}

import scala.concurrent.duration.Duration

object SchedulerAlert {

  def apply[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](fields: List[String],
                                                            grabberPipes: List[EnvironmentPipe[F]],
                                                            alertCondition: AlertCondition[F],
                                                            time: Duration): EnvironmentPipe[F] = EnvironmentPipe[F] { (envPipe: PipeEnv) => {
    def commonPipe = grabberPipes.tail.foldLeft(grabberPipes.head) {
      case (acc, nextPipe) => acc.combine(nextPipe)
    }

    val res = for {
      resultEnv <- commonPipe.commonFunc(envPipe)
      _ <- NotificationAlert(alertCondition).compile(resultEnv)
    } yield Monoid[PipeEnv].combine(envPipe, resultEnv)

    res
  }}
}
