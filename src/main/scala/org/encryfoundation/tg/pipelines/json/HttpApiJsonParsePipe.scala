package org.encryfoundation.tg.pipelines.json

import canoe.api.Scenario
import org.encryfoundation.tg.pipelines.{Pipe, PipeCompanion}
import org.encryfoundation.tg.services.Explorer
import org.http4s.{Method, Request, Uri}
import shapeless.HList

final class HttpApiJsonParsePipe[F[_]] private (schema: Schema,
                                                url: String,
                                                explorer: Explorer[F])
                                                (interpret: Scenario[F, HList]) extends Pipe[F, Any, HList](interpret)

object HttpApiJsonParsePipe extends PipeCompanion {
  def apply[F[_]](schema: Schema,
                  url: String,
                  explorer: Explorer[F]): Pipe[F, Any, HList] = {
    val request = Request[F](
      Method.GET,
      Uri.unsafeFromString(s"$url")
    )

    new HttpApiJsonParsePipe(schema, url, explorer)(
      Scenario.eval(explorer.makeGetRequest[HList](request)(schema.decoder))
    )
  }

  override val name: String = "HttpApiJsonParsePipe"
}
