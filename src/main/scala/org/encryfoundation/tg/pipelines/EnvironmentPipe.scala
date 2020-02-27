package org.encryfoundation.tg.pipelines

import canoe.api.Scenario
import cats.Applicative
import cats.implicits._

trait EnvironmentPipe[F[_]] extends Pipe[F, PipeEnv, PipeEnv] {
  val appl: Applicative[F]
  def commonFunc: PipeEnv => Scenario[F, PipeEnv]
  def combine[O2 <: PipeEnv](nextPipe: EnvironmentPipe[F]): EnvironmentPipe[F] = EnvironmentPipe[F](
    commonFunc.andThen(_.flatMap(res => nextPipe.commonFunc(res)))
  )(appl)
}

object EnvironmentPipe {
  def apply[F[_]](envPipe: PipeEnv => Scenario[F, PipeEnv])(implicit a: Applicative[F]): EnvironmentPipe[F] =
    new EnvironmentPipe[F] {
      val appl: Applicative[F] = a
      override def commonFunc: PipeEnv => Scenario[F, PipeEnv] = envPipe
      override def run: Scenario[F, PipeEnv] = Scenario.eval(PipeEnv.empty.pure[F])
    }
}


