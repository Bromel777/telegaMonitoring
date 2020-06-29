package org.encryfoundation.tg.pipelines

import canoe.api.{Scenario, _}
import canoe.models.outgoing.TextContent
import cats.Applicative
import cats.implicits._
import cats.mtl.MonadState
import org.encryfoundation.tg.data.Errors.BotError
import org.encryfoundation.tg.env.BotEnv

trait EnvironmentPipe[F[_]] extends Pipe[F, PipeEnv, PipeEnv] {
  def commonFunc: PipeEnv => Scenario[F, PipeEnv]
  def compile: PipeEnv => Scenario[F, Unit]
  def combine[O2 <: PipeEnv](nextPipe: EnvironmentPipe[F])(implicit a: Applicative[F], m: MonadState[F, BotEnv[F]]): EnvironmentPipe[F] = EnvironmentPipe[F](
    commonFunc.andThen(_.flatMap(res => nextPipe.commonFunc(res)))
  )
}

object EnvironmentPipe {

  def apply[F[_]: MonadState[*[_], BotEnv[F]]](envPipe: PipeEnv => Scenario[F, PipeEnv])(implicit a: Applicative[F]): EnvironmentPipe[F] =
    new EnvironmentPipe[F] {
      val cancelMessage = "cancel"
      override def commonFunc: PipeEnv => Scenario[F, PipeEnv] = envPipe
      override def run: Scenario[F, PipeEnv] = Scenario.eval(PipeEnv.empty.pure[F])
      override def compile: PipeEnv => Scenario[F, Unit] = (env: PipeEnv) => envPipe(env)
        .handleErrorWith {
          case err: BotError => for {
            botEnv <- Scenario.eval(MonadState[F, BotEnv[F]].get)
            _ <- Scenario.eval(err.chat.send(TextContent(s"Error during pipeline: ${err.msgToChat}"))(botEnv.tgClient.get))
          } yield ()
        } >> Scenario.done
    }
}


