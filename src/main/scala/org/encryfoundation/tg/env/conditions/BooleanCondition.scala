package org.encryfoundation.tg.env.conditions

import cats.Applicative
import org.encryfoundation.tg.pipelines.PipeEnv
import org.encryfoundation.tg.pipelines.json.Value
import cats.syntax.applicative._

object BooleanCondition {

  def apply[F[_]: Applicative](filter: List[Value] => List[Value], condition: List[Value] => Boolean): AlertCondition[F] = new AlertCondition[F] {
    override def check(env: PipeEnv): F[Boolean] = (filter andThen condition)(env.variables.values.toList).pure[F]
  }
}
