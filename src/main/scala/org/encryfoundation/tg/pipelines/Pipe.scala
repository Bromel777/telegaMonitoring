package org.encryfoundation.tg.pipelines

import canoe.api.Scenario
import cats.kernel.Semigroup
import canoe.api.Scenario
import cats.data.State
import cats.effect.IO
import cats.tagless.{autoFunctorK, finalAlg}
import cats.~>
import org.encryfoundation.tg.env.BotEnv

trait Pipe[F[_], -I, +O] {

  def run: Scenario[F, O]

  def map[O2, I2](fn: O => O2): Pipe[F, I, O2] = flatMap(fn.andThen(Pipe.pure[F, I2, O2]))

  def flatMap[O2, I2 <: I](fn: O => Pipe[F, I2, O2]): Pipe[F, I, O2] = Pipe[F, I, O2](run.flatMap(fn(_).run))

  def combine[O2](nextPipe: Pipe[F, O, O2]): Pipe[F, I, O2] = Pipe[F, I, O2](run.flatMap(_ => nextPipe.run))
}

object Pipe {

  def apply[F[_], I, O](interpret: Scenario[F, O]): Pipe[F, I, O] = new Pipe[F, I, O] {
    override def run: Scenario[F, O] = interpret
  }

  def pure[F[_], I, O](a: O) = Pipe[F, I, O](Scenario.pure(a))
}



