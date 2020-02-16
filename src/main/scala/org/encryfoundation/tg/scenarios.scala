package org.encryfoundation.tg

import canoe.api.{Scenario, TelegramClient, _}
import canoe.models.Chat
import canoe.syntax._
import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.implicits._
import io.circe.generic.auto._
import io.circe.syntax._
import org.encryfoundation.tg.config.BotConfig
import org.encryfoundation.tg.services.Explorer

object scenarios {

  def nodeStatusMonitoring[F[_]: TelegramClient](explorer: Explorer[F], config: BotConfig) = for {
    chat <- Scenario.expect(command("nodestatus").chat)
    nodeInfo <- Scenario.eval(explorer.getInfo)
    _ <- Scenario.eval(chat.send(nodeInfo.asJson.toString()))
  } yield ()

  def chainMonitoring[F[_]: TelegramClient](explorer: Explorer[F], config: BotConfig) = for {
    chat <- Scenario.expect(command("chainstatus").chat)
    nodesStatus <- Scenario.eval(explorer.nodesStatus)
    _ <- Scenario.eval(chat.send(nodesStatus.map { case (isActive, ip) =>
        val status = if (isActive) '\u2705' else '\u274C'
        List(status, ip).mkString(" ")
    }.mkString("\n ")))
  } yield ()

  def startNodeMonitoring[F[_]: TelegramClient: Sync](explorer: Explorer[F],
                                                      config: BotConfig,
                                                      prevRes: Ref[F, Map[String, Boolean]]) = for {
    chat <- Scenario.expect(command("startmonitoring").chat)
    _ <- Scenario.eval(recurMonitoring(explorer, config, prevRes, chat))
  } yield ()

  def recurMonitoring[F[_]: TelegramClient: Sync](explorer: Explorer[F],
                                                  config: BotConfig,
                                                  prevRes: Ref[F, Map[String, Boolean]],
                                                  chat: Chat): F[Unit] = for {
    prevMap <- prevRes.get
    nodesStatus <- explorer.nodesStatus
    _  <- nodesStatus.traverse { case (isActive, ip) =>
      if (prevMap.get(ip).get != isActive) {
        chat.send(s"Node $ip die :((((") >> prevRes.update(_.updated(ip, isActive))
      } else ().pure[F]
    }
    _ <- recurMonitoring(explorer, config, prevRes, chat)
  } yield ()
}
