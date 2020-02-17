package org.encryfoundation.tg

import canoe.api.{Scenario, TelegramClient, _}
import canoe.models.Chat
import canoe.syntax._
import cats.Applicative
import cats.effect.concurrent.Ref
import cats.effect.{Sync, Timer}
import cats.implicits._
import io.circe.generic.auto._
import io.circe.syntax._
import org.encryfoundation.tg.config.BotConfig
import org.encryfoundation.tg.data.Errors.BotError
import org.encryfoundation.tg.repositories.UserRepository
import org.encryfoundation.tg.services.{Explorer, UserService}

import scala.concurrent.duration._

object scenarios {

  def handleErrInScenario[F[_]: TelegramClient: Applicative]: Scenario[F, Unit] => Scenario[F, Unit] = unhandled =>
    unhandled.handleErrorWith {
      case botError: BotError => Scenario.eval(botError.chat.send(botError.msgToChat.toString)) >> Scenario.eval(().pure[F])
      case _: Throwable => Scenario.eval(().pure[F])
    }

  def nodeStatusMonitoring[F[_]: TelegramClient: Sync](explorer: Explorer[F],
                                                       config: BotConfig,
                                                       userRepository: UserRepository[F],
                                                       userService: UserService[F]) = for {
    chat <- Scenario.expect(command("nodestatus").chat)
    _  <- Scenario.eval(userService.verifyUser(chat))
    nodeInfo <- Scenario.eval(explorer.getInfo)
    _ <- Scenario.eval(chat.send(nodeInfo.asJson.toString()))
  } yield ()

  def chainMonitoring[F[_]: TelegramClient](explorer: Explorer[F],
                                            config: BotConfig,
                                            userService: UserService[F]) = for {
    chat <- Scenario.expect(command("chainstatus").chat)
    _  <- Scenario.eval(userService.verifyUser(chat))
    nodesStatus <- Scenario.eval(explorer.nodesStatus)
    _ <- Scenario.eval(chat.send(nodesStatus.map { case (isActive, ip) =>
        val status = if (isActive) '\u2705' else '\u274C'
        List(status, ip).mkString(" ")
    }.mkString("\n ")))
  } yield ()


  def startNodeMonitoring[F[_]: TelegramClient: Sync: Timer](explorer: Explorer[F],
                                                             config: BotConfig,
                                                             prevRes: Ref[F, Map[String, Boolean]],
                                                             userService: UserService[F]) = for {
    chat <- Scenario.expect(command("startmonitoring").chat)
    _  <- Scenario.eval(userService.verifyUser(chat))
    _ <- Scenario.eval(recurMonitoring(explorer, config, prevRes, chat))
  } yield ()

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
    _ <- Timer[F].sleep(5 seconds) >> recurMonitoring(explorer, config, prevRes, chat)
  } yield ()

  def registerUser[F[_]: TelegramClient: Sync](userService: UserService[F]) = for {
    chat <- Scenario.expect(command("register").chat)
    _ <- Scenario.eval(userService.checkPossibilityToRegister(chat))
    _ <- Scenario.eval(chat.send("Enter username"))
    username <- Scenario.expect(text)
    _ <- Scenario.eval(chat.send("Enter pass"))
    pass <- Scenario.expect(text)
    _ <- Scenario.eval(userService.registerUser(chat, pass))
    _ <- Scenario.eval(chat.send(s"Hello, $username"))
  } yield ()
}
