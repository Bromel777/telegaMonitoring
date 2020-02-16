package org.encryfoundation.tg

import canoe.api.{Scenario, TelegramClient, _}
import canoe.syntax._
import io.circe.generic.auto._
import io.circe.syntax._
import org.encryfoundation.tg.config.BotConfig
import org.encryfoundation.tg.services.Explorer

object scenarios {

  def nodeStatusMonitoring[F[_]: TelegramClient](explorer: Explorer[F], config: BotConfig) = for {
    chat <- Scenario.start(command("nodestatus").chat)
    nodeInfo <- Scenario.eval(explorer.getInfo)
    _ <- Scenario.eval(chat.send(nodeInfo.asJson.toString()))
  } yield ()

  def chainMonitoring[F[_]: TelegramClient](explorer: Explorer[F], config: BotConfig) = for {
    chat <- Scenario.start(command("chainstatus").chat)
    nodesStatus <- Scenario.eval(explorer.nodesStatus)
    _ <- Scenario.eval(chat.send(nodesStatus.map { case (isActive, ip) =>
        val status = if (isActive) '\u2705' else '\u274C'
        List(status, ip).mkString(" ")
    }.mkString("\n ")))
  } yield ()

}
