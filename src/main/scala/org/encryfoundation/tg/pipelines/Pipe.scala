package org.encryfoundation.tg.pipelines

import canoe.api.Scenario
import cats.kernel.Semigroup
import canoe.api.Scenario
import cats.data.State
import cats.effect.IO
import cats.tagless.{autoFunctorK, finalAlg}
import cats.~>
import org.encryfoundation.tg.env.BotEnv

class Pipe[F[_], -I, +O](val interpret: Scenario[F, O]) {

  def run: Scenario[F, O] = interpret

  def map[O2, I2](fn: O => O2): Pipe[F, I, O2] = flatMap(fn.andThen(Pipe.pure[F, I2, O2]))

  def flatMap[O2, I2](fn: O => Pipe[F, I2, O2]): Pipe[F, I, O2] = new Pipe[F, I, O2](interpret.flatMap(fn(_).interpret))

  def combine[O2](nextPipe: Pipe[F, O, O2]): Pipe[F, I, O2] = new Pipe[F, I, O2](interpret.flatMap(_ => nextPipe.interpret))
}

object Pipe {

  def pure[F[_], I, O](a: O) = new Pipe[F, I, O](Scenario.pure(a))
}



