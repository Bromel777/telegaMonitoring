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
  case class NotRegisteredChat(chat: Chat) extends BotError {
    override val msgToChat: String = "You are not registered in this chat!"
  }
  case class PipeErr(chat: Chat) extends BotError {
    override val msgToChat: String = "Error in pipe!"
  }
  case class ConditionErr(chat: Chat, conditionMsg: String) extends BotError {
    override val msgToChat: String = s"Condition error in pipe: $conditionMsg"
  }
}
