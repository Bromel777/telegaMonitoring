package org.encryfoundation.tg.commands

import canoe.api.{Scenario, TelegramClient, _}
import canoe.models.Chat
import canoe.syntax._
import cats.Monad
import cats.implicits._
import org.encryfoundation.tg.data.Errors.BotError
import org.encryfoundation.tg.services.AuthService

trait Command[F[_]] {
  val invokeName: String
  def scenario: Scenario[F, Unit]
}

object Command {

  private def handleErrInScenario[F[_]: TelegramClient: Monad](unhandled: Scenario[F, Unit], chat: Chat) =
    unhandled.handleErrorWith {
      case botError: BotError => Scenario.eval(botError.chat.send(botError.msgToChat.toString)) >> Scenario.eval(().pure[F])
      case _: Throwable => Scenario.eval(().pure[F])
    }

  def make[F[_]: TelegramClient: Monad](name: String)(body: Chat => Scenario[F, Unit]): Command[F] = new Command[F] {
    override val invokeName: String = name
    override val scenario: Scenario[F, Unit] = Scenario.expect(command(name).chat).flatMap(chat =>
      handleErrInScenario(body(chat), chat)
    )
  }

  def makeAuth[F[_]: TelegramClient: Monad](name: String)(body: Chat => Scenario[F, Unit])(authService: AuthService[F]): Command[F] =
    make(name)(chat => Scenario.eval(authService.isRegistered(chat)) >> body(chat))
}
