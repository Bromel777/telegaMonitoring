package org.encryfoundation.tg.env.conditions

import org.encryfoundation.tg.pipelines.PipeEnv

trait AlertCondition[F[_]] {
  def check(env: PipeEnv): F[Boolean]
}
