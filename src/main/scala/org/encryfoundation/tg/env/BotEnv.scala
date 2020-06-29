package org.encryfoundation.tg.env

import canoe.api.TelegramClient
import canoe.models.Chat
import org.encryfoundation.tg.services.ExplorerService

case class BotEnv[F[_]](tgClient: Option[TelegramClient[F]],
                        explorer: ExplorerService[F])
