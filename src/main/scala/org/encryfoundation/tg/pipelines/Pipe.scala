package org.encryfoundation.tg.pipelines

import canoe.api.Scenario

trait Pipe[F[_], -I, +O] {

  def run: Scenario[F, O]

  def map[O2, I2](fn: O => O2): Pipe[F, I, O2] = flatMap(fn.andThen(Pipe.pure[F, I2, O2]))

  def flatMap[O2, I2 <: I](fn: O => Pipe[F, I2, O2]): Pipe[F, I, O2] = Pipe[F, I, O2](run.flatMap(fn(_).run))
}

object Pipe {

  def apply[F[_], I, O](interpret: Scenario[F, O]): Pipe[F, I, O] = new Pipe[F, I, O] {
    override def run: Scenario[F, O] = interpret
  }

  def pure[F[_], I, O](a: O) = Pipe[F, I, O](Scenario.pure(a))
}



