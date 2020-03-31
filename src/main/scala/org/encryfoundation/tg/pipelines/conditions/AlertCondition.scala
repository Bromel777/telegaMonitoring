package org.encryfoundation.tg.pipelines.conditions

import org.encryfoundation.tg.pipelines.PipeEnv

trait AlertCondition[F[_]] {
  def check(env: PipeEnv): F[Boolean]
  def msg(env: PipeEnv): String
}
