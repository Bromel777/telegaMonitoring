package org.encryfoundation.tg.pipelines.json

import canoe.api.Scenario
import cats.Applicative
import org.encryfoundation.tg.pipelines.{EnvironmentPipe, Pipe, PipeEnv}
import org.encryfoundation.tg.services.Explorer
import org.http4s.{Method, Request, Uri}
import shapeless.HList

object HttpApiJsonParsePipe {

  def apply[F[_]: Applicative](schema: Schema,
                               url: String,
                               explorer: Explorer[F]): Pipe[F, PipeEnv, PipeEnv] = {
    val request = Request[F](
      Method.GET,
      Uri.unsafeFromString(s"$url")
    )

    EnvironmentPipe[F]( (env: PipeEnv) =>
      for {
        res <- Scenario.eval(explorer.makeGetRequest[HList](request)(schema.decoder))
      } yield (env.copy(variables = env.variables + ("json" -> res.toString)))
    )
  }
}
