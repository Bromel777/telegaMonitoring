package org.encryfoundation.tg.pipelines

import canoe.api.Scenario
import cats.kernel.Semigroup

class Pipe[F[_], I, O](interpret: Scenario[F, O]) {
  def flatmap[O2](fn: O => Pipe[F, I, O2]): Pipe[F, I, O2] = new Pipe[F, I, O2](interpret.flatMap(fn(_).interpret))
}

