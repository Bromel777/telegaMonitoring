package org.encryfoundation.tg.pipelines

import canoe.models.Chat
import cats.Applicative
import cats.kernel.Monoid
import com.typesafe.scalalogging.StrictLogging
import org.encryfoundation.tg.pipelines.json.{JsonType, Value}

sealed trait PipeEnv {
  val variables: Map[String, Value]
  val chat: Option[Chat]
  def addToEnv(name: String, value: Value): PipeEnv
}

case class PredefinedByNamePipeEnv(variables: Map[String, Value],
                                   chat: Option[Chat],
                                   nameStack: List[String]) extends PipeEnv with StrictLogging {

  override def addToEnv(name: String, value: Value): PipeEnv = {
    this.copy(
      variables + (nameStack.head -> value.copy(name = nameStack.head)),
      chat,
      nameStack.tail
    )
  }
}

object PipeEnv {

  implicit object pipeMonoid extends Monoid[PipeEnv] {
    override def empty: PipeEnv = PipeEnv(Map.empty, None)

    override def combine(x: PipeEnv, y: PipeEnv): PipeEnv = PipeEnv(x.variables ++ y.variables, x.chat)
  }

  def apply(vars: Map[String, Value], chatId: Option[Chat]): PipeEnv = new PipeEnv {
    override val variables: Map[String, Value] = vars
    override val chat: Option[Chat] = chatId
    override def addToEnv(name: String, value: Value): PipeEnv = PipeEnv(
      variables + (name -> value),
      this.chat
    )
  }

  val empty = PipeEnv(Map.empty, None)
}
