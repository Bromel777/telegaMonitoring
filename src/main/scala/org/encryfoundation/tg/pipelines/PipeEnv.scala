package org.encryfoundation.tg.pipelines

import canoe.models.Chat
import cats.Applicative
import cats.kernel.Monoid

case class PipeEnv(variables: Map[String, String],
                   chat: Option[Chat])

object PipeEnv {

  implicit object pipeMonoid extends Monoid[PipeEnv] {
    override def empty: PipeEnv = PipeEnv(Map.empty, None)

    override def combine(x: PipeEnv, y: PipeEnv): PipeEnv = PipeEnv(x.variables ++ y.variables, x.chat)
  }

  val empty = PipeEnv(Map.empty, None)
}
