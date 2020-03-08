package org.encryfoundation.tg.pipelines.json

import canoe.api.Scenario
import cats.Applicative
import cats.mtl.MonadState
import org.encryfoundation.tg.env.BotEnv
import org.encryfoundation.tg.pipelines.{EnvironmentPipe, Pipe, PipeEnv}
import org.encryfoundation.tg.services.Explorer
import org.http4s.{Method, Request, Uri}
import shapeless.{HList, Lub}

object HttpApiJsonParsePipe {

  def apply[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](schema: Schema = Schema.empty,
                                                            url: String): EnvironmentPipe[F] = {
    val request = Request[F](
      Method.GET,
      Uri.unsafeFromString(s"$url")
    )

    EnvironmentPipe[F]( (env: PipeEnv) =>
      for {
        botEnv <- Scenario.eval(MonadState[F, BotEnv[F]].get)
        res <- Scenario.eval(botEnv.explorer.makeGetRequest[HList](request)(schema.decoder))
      } yield env.copy(variables = env.variables + ("json" -> res.toString))
    )
  }
}
