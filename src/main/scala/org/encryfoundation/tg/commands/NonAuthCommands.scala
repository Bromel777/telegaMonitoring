package org.encryfoundation.tg.commands

import canoe.api.models.Keyboard.Reply
import canoe.api.{Scenario, TelegramClient, _}
import canoe.models.{KeyboardButton, ReplyKeyboardMarkup}
import canoe.syntax.{text, _}
import cats.effect.Sync
import cats.implicits._
import org.encryfoundation.tg.services.{AuthService, UserService}

object NonAuthCommands {

  val dummyKeyBoard = ReplyKeyboardMarkup(
    keyboard = List(List(KeyboardButton.text("hello"))), oneTimeKeyboard = true.some
  )

  def commandsMenu[F[_]](commands: List[Command[F]]) = ReplyKeyboardMarkup(
    keyboard = List(commands.map(command => KeyboardButton.text(s"/${command.invokeName}"))), oneTimeKeyboard = true.some
  )


  def registerUser[F[_]: TelegramClient: Sync](authService: AuthService[F],
                                               userService: UserService[F]) =
    Command.make("register")(chat =>
      for {
        _ <- Scenario.eval(authService.checkPossibilityToRegister(chat))
        _ <- Scenario.eval(chat.send("Enter username"))
        username <- Scenario.expect(text)
        _ <- Scenario.eval(chat.send("Enter pass"))
        pass <- Scenario.expect(text)
        _ <- Scenario.eval(authService.registerUser(chat, pass))
        _ <- Scenario.eval(userService.updateLogin(username))
        _ <- Scenario.eval(chat.send(s"Hello, $username"))
      } yield ()
    )

  def sendInfo[F[_]: TelegramClient: Sync](userService: UserService[F]) =
    Command.make("info")(chat =>
      for {
        login <- Scenario.eval(userService.getLogin)
        _ <- Scenario.eval(chat.send(s"You login: $login"))
      } yield ()
    )

  def menu[F[_]: TelegramClient: Sync](authService: AuthService[F], commands: List[Command[F]]) =
    Command.make("menu")(chat =>
      for {
        isAuth <- Scenario.eval(authService.isAuth(chat))
        commandsForMenu <- Scenario.eval {
          if (isAuth) commands.pure[F] else commands.filter(_.isAuth == false).pure[F]
        }
        _ <- Scenario.eval(chat.send(s"Accepatable menu", keyboard = Reply(commandsMenu(commandsForMenu))))
      } yield ()
    )
}
