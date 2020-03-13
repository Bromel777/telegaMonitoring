package org.encryfoundation.tg.env

import org.encryfoundation.tg.pipelines.PipeEnv

trait AlertCondition {
  def check[F[_]](env: PipeEnv): F[Boolean]
}
