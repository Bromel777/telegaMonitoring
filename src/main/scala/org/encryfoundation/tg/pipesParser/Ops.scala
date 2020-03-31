package org.encryfoundation.tg.pipesParser

import fastparse._
import org.encryfoundation.tg.pipesParser.Ops.Operator._

object Ops {

  sealed trait Operator
  object Operator {
    case object Eq extends Operator
    case object Gt extends Operator
    case object Lt extends Operator
  }

  def op[_: P](operatorStr: => P[Unit], operator: Operator): P[Operator] = P(operatorStr.!).map(_ => operator)
  def eq[_: P]: P[Operator] = op("==", Eq)
  def gt[_: P]: P[Operator] = op(">", Gt)
  def lt[_: P]: P[Operator] = op("<", Lt)
  def boolOp[_: P]: P[Operator] = P(eq | gt | lt)
}
