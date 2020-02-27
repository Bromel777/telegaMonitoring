package org.encryfoundation.tg.pipelines

import canoe.models.Chat

case class PipeEnv(variables: Map[String, String],
                   chat: Option[Chat])

object PipeEnv {
  val empty = PipeEnv(Map.empty, None)
}
