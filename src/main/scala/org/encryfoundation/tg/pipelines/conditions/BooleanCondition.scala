package org.encryfoundation.tg.pipelines.conditions

import cats.Applicative
import org.encryfoundation.tg.pipelines.PipeEnv
import org.encryfoundation.tg.pipelines.json.Value
import cats.syntax.applicative._
import org.encryfoundation.tg.pipesParser.Ops.Operator

object BooleanCondition {

  def apply[F[_]: Applicative](filter: List[Value] => List[Value],
                               condition: List[Value] => Boolean,
                               op: Operator): AlertCondition[F] = new AlertCondition[F] {
    override def check(env: PipeEnv): F[Boolean] = (filter andThen condition)(env.variables.values.toList).pure[F]
    override def msg(env: PipeEnv): String = s"Boolean condition failed! " +
      s"${filter(env.variables.values.toList).map(_.name).mkString(op.toString)} == false"
  }
}
