package org.encryfoundation.tg.commands

import canoe.api.{Scenario, TelegramClient, _}
import canoe.models.Chat
import canoe.syntax._
import io.circe.generic.auto._
import io.circe.syntax._
import cats.Monad
import cats.effect.{Sync, Timer}
import cats.effect.concurrent.Ref
import org.encryfoundation.tg.config.BotConfig
import org.encryfoundation.tg.repositories.UserRepository
import org.encryfoundation.tg.services.{AuthService, Explorer, UserService}

object AuthCommands {

  def logoutPipeline[F[_]: TelegramClient: Sync](authService: AuthService[F], userService: UserService[F]) =
    Command.makeAuth("logout")(chat =>
      for {
        _ <- Scenario.eval(authService.logout(chat))
        _ <- Scenario.eval(userService.updateLogin("Unknown bird"))
      } yield ()
    )(authService)

  def nodeStatusMonitoring[F[_]: TelegramClient: Sync](explorer: Explorer[F],
                                                       config: BotConfig,
                                                       userRepository: UserRepository[F],
                                                       userService: AuthService[F]): Command[F] =
    Command.makeAuth("nodestatus")(chat =>
      for {
        nodeInfo <- Scenario.eval(explorer.getInfo)
        _ <- Scenario.eval(chat.send(nodeInfo.asJson.toString()))
      } yield ()
    )(userService)


  def chainMonitoring[F[_]: TelegramClient: Monad](explorer: Explorer[F],
                                                   config: BotConfig,
                                                   authService: AuthService[F]) =
    Command.makeAuth("chainstatus")(chat =>
      for {
        nodesStatus <- Scenario.eval(explorer.nodesStatus)
        _ <- Scenario.eval(chat.send(nodesStatus.map { case (isActive, ip) =>
          val status = if (isActive) '\u2705' else '\u274C'
          List(status, ip).mkString(" ")
        }.mkString("\n")))
      } yield ()
    )(authService)

  def startNodeMonitoring[F[_]: TelegramClient: Sync: Timer](explorer: Explorer[F],
                                                             config: BotConfig,
                                                             prevRes: Ref[F, Map[String, Boolean]],
                                                             authService: AuthService[F]) =
    Command.makeAuth("startmonitoring")(chat =>
      for {
        _ <- Scenario.eval(recurMonitoring(explorer, config, prevRes, chat))
      } yield ()
    )(authService)

  def recurMonitoring[F[_]: TelegramClient: Sync: Timer](explorer: Explorer[F],
                                                         config: BotConfig,
                                                         prevRes: Ref[F, Map[String, Boolean]],
                                                         chat: Chat): F[Unit] = for {
    prevMap <- prevRes.get
    nodesStatus <- explorer.nodesStatus
    _  <- nodesStatus.traverse { case (isActive, ip) =>
      if (prevMap(ip) != isActive && prevMap(ip)) {
        chat.send(s"Node $ip die :((((") >> prevRes.update(map => map + (ip -> isActive))
      } else if (prevMap(ip) != isActive && !prevMap(ip)) {
        chat.send(s"Node $ip alive :)))") >> prevRes.update(map => map + (ip -> isActive))
      } else {
        ().pure[F]
      }
    }
    _ <- Timer[F].sleep(15 seconds) >> recurMonitoring(explorer, config, prevRes, chat)
  } yield ()
}
