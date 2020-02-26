package org.encryfoundation.tg.pipelines.chat

import canoe.api.Scenario
import canoe.syntax.{text, _}
import org.encryfoundation.tg.pipelines.{Pipe}

final class ReadPipe[F[_]] private (interpret: Scenario[F, String]) extends Pipe[F, Any, String](interpret) {
}

object ReadPipe {
  def apply[F[_]](): Pipe[F, Any, String] = new ReadPipe(Scenario.expect(text))

}


