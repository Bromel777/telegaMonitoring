package org.encryfoundation.tg.data

import canoe.models.Chat

object Errors {

  trait BotError extends Throwable {
    val chat: Chat
    val msgToChat: String
  }
  case class NotAuthUserError(chat: Chat) extends BotError {
    override val msgToChat: String = "You are not registered! Use /register"
  }
  case class DuplicateAuth(chat: Chat) extends BotError {
    override val msgToChat: String = "You are registered!"
  }
  case class IncorrectPassword(chat: Chat) extends BotError {
    override val msgToChat: String = "Password is incorrect!"
  }
}
