package org.encryfoundation.tg.pipelines.chat

import canoe.api.{Scenario, TelegramClient, _}
import canoe.models.Chat
import canoe.syntax._
import cats.mtl.MonadState
import org.encryfoundation.tg.env.BotEnv
import org.encryfoundation.tg.pipelines.{Pipe, PipeCompanion}
import cats.implicits._

final class InvokePipe[F[_]] private (name: String)
                                     (interpret: Scenario[F, Chat]) extends Pipe[F, Any, Chat](interpret)

object InvokePipe extends PipeCompanion {

  def apply[F[_]: MonadState[*[_], BotEnv[F]]](name: String): Pipe[F, Any, Chat] =
    new InvokePipe(name)(
      for {
        botChat <- Scenario.expect(command( name ).chat)
        _ <- Scenario.eval(MonadState[F, BotEnv[F]].modify(prevState => prevState.copy[F](chat = botChat.some)))
      } yield botChat
    )

  override val name: String = "InvokePipe"
}
