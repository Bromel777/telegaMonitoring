package org.encryfoundation.tg.pipelines

import canoe.api.Scenario

trait Pipe[F[_], T] {
  def interpret: Scenario[F, T]
}