package org.encryfoundation.tg.pipelines.conditions

import cats.Applicative
import org.encryfoundation.tg.pipelines.PipeEnv
import cats.syntax.applicative._

object ValueCondition {
  def apply[F[_]: Applicative](values: List[String]): AlertCondition[F] = new AlertCondition[F] {
    override def check(env: PipeEnv): F[Boolean] = (!values.map(env.variables(_)).contains("true")).pure[F]
    override def msg(env: PipeEnv): String = "Value condition failed!"
  }
}
