package org.encryfoundation.tg.pipelines.chat

import canoe.api.Scenario
import canoe.syntax.{text, _}
import cats.Applicative
import cats.mtl.MonadState
import cats.implicits._
import cats.syntax._
import org.encryfoundation.tg.env.BotEnv
import org.encryfoundation.tg.pipelines.Pipe

final class ReadPipe[F[_]] private (interpret: Scenario[F, Unit]) extends Pipe[F, Any, Unit](interpret) {
}

object ReadPipe {
  def apply[F[_]: Applicative](varName: String)(implicit a: MonadState[F, BotEnv[F]]): Pipe[F, Any, Unit] =
    new ReadPipe(
      for {
        text <- Scenario.expect(text)
        _ <- Scenario.eval(a.modify(map => map.copy(vars = map.vars + (varName -> text))))
        _ <- Scenario.eval(println("put").pure[F])
      } yield ()
    )
}


