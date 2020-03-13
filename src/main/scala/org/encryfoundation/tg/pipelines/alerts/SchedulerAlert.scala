package org.encryfoundation.tg.pipelines.alerts

import cats.Applicative
import cats.effect.ContextShift
import cats.kernel.Monoid
import cats.mtl.MonadState
import org.encryfoundation.tg.env.{AlertCondition, BotEnv}
import org.encryfoundation.tg.pipelines.{EnvironmentPipe, Pipe, PipeEnv}

import scala.concurrent.duration.Duration

object SchedulerAlert {

  def apply[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](fields: List[String],
                                                            grabberPipes: List[PipeEnv => EnvironmentPipe[F]],
                                                            alertCondition: AlertCondition,
                                                            time: Duration): EnvironmentPipe[F] = new EnvironmentPipe[F] { (envPipe: PipeEnv) => {
    def grabFields = grabberPipes.tail.foldLeft[Pipe[F, PipeEnv, PipeEnv]](grabberPipes.head(envPipe)) {
      case (commonPipe, nextGrabPipe) => commonPipe.flatMap(res => nextGrabPipe(res))
    }

    val alertPipe = for {
      resultEnv <- grabFields
      _ <- NotificationAlert(alertCondition)
    } yield resultEnv
  }}
}
