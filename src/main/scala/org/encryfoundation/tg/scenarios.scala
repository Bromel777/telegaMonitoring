package org.encryfoundation.tg

import canoe.api.{Scenario, TelegramClient, _}
import canoe.syntax._
import io.circe.generic.auto._
import io.circe.syntax._
import org.encryfoundation.tg.config.BotConfig
import org.encryfoundation.tg.services.Explorer

object scenarios {

  def nodeStatusMonitoring[F[_]: TelegramClient](explorer: Explorer[F], config: BotConfig) = for {
    chat <- Scenario.start(command("nodeStatus").chat)
    nodeInfo <- Scenario.eval(explorer.getInfo)
    _ <- Scenario.eval(chat.send(nodeInfo.asJson.toString()))
  } yield ()

}
