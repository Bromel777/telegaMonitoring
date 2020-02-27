package org.encryfoundation.tg.pipelines.chat

import canoe.api.{Scenario, TelegramClient, _}
import canoe.models.Chat
import canoe.models.outgoing.TextContent
import canoe.syntax._
import cats.Applicative
import cats.mtl.MonadState
import org.encryfoundation.tg.env.BotEnv
import org.encryfoundation.tg.pipelines.{EnvironmentPipe, Pipe, PipeEnv}
import cats.implicits._

object InvokePipe {

  def apply[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](name: String): EnvironmentPipe[F] =
    EnvironmentPipe[F]( (prevEnv: PipeEnv) =>
      for {
        botChat <- Scenario.expect(command(name).chat)
      } yield prevEnv.copy(chat = botChat.some)
    )
}
