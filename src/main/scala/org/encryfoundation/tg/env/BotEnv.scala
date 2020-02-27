package org.encryfoundation.tg.env

import canoe.api.TelegramClient
import canoe.models.Chat

case class BotEnv[F[_]](tgClient: Option[TelegramClient[F]],
                        chat: Option[Chat],
                        vars: Map[String, String] = Map.empty)
