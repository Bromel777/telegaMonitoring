package org.encryfoundation.tg.pipelines.exprInterpreter

import cats.Order
import cats.syntax.order._
import org.encryfoundation.tg.pipesParser.Ops.Operator

object BooleanOpInterpreter {

  def interper[T: Order](arg1: T, arg2: T, op: Operator): Boolean = op match {
    case Operator.Eq => Order[T].eqv(arg1, arg2)
    case Operator.Gt => Order[T].gt(arg1, arg2)
    case Operator.Lt => Order[T].lt(arg1, arg2)
  }
}
