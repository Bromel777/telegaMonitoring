package org.encryfoundation.tg.pipelines.alerts

import canoe.api.Scenario
import cats.{Applicative, Monad}
import cats.effect.Timer
import cats.mtl.MonadState
import canoe.models.PrivateChat
import canoe.models.messages.{TelegramMessage, TextMessage}
import canoe.syntax._
import cats.syntax.applicative._
import cats.syntax.foldable._
import cats.syntax.parallel._
import cats.instances.list._
import cats.kernel.Monoid
import com.typesafe.scalalogging.StrictLogging
import org.encryfoundation.tg.data.Errors.{BotError, PipeErr}
import org.encryfoundation.tg.env.BotEnv
import org.encryfoundation.tg.pipelines.conditions.AlertCondition
import org.encryfoundation.tg.pipelines.{EnvironmentPipe, Pipe, PipeEnv, PredefinedByNamePipeEnv}
import retry.{RetryDetails, RetryPolicies, retryingM}
import tofu.Raise

import scala.concurrent.duration.{Duration, FiniteDuration}

object SchedulerAlert extends StrictLogging {

  def apply[F[_]: MonadState[*[_], BotEnv[F]]: Timer: Monad: Raise[*[_], Throwable]]
    (fields: List[String],
     grabberPipes: List[EnvironmentPipe[F]],
     alertCondition: AlertCondition[F],
     time: FiniteDuration
    ): EnvironmentPipe[F] = EnvironmentPipe[F]{ (envPipe: PipeEnv) => {

      val predefEnv = envPipe match {
        case pred: PredefinedByNamePipeEnv => pred
        case env: PipeEnv => PredefinedByNamePipeEnv(envPipe.variables, env.chat, fields)
      }

      def commonPipe: EnvironmentPipe[F] = grabberPipes.tail.foldLeft(grabberPipes.head) {
        case (acc, nextPipe) => acc.combine(nextPipe)
      }

      def scheduler: EnvironmentPipe[F] = commonPipe.combine(NotificationAlert[F](alertCondition))

      def prog: Scenario[F, PipeEnv] = for {
        res <- scheduler.commonFunc(predefEnv)
        _ <- Scenario.eval(Timer[F].sleep(time)) >> prog
      } yield res

      prog
    }}
}
