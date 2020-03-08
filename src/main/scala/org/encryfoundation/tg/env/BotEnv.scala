package org.encryfoundation.tg.env

import canoe.api.TelegramClient
import canoe.models.Chat
import org.encryfoundation.tg.services.Explorer

case class BotEnv[F[_]](tgClient: Option[TelegramClient[F]],
                        explorer: Explorer[F])
