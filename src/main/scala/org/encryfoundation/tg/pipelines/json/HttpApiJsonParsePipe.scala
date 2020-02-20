package org.encryfoundation.tg.pipelines.json

import canoe.api.Scenario
import org.encryfoundation.tg.pipelines.Pipe
import org.encryfoundation.tg.services.Explorer
import org.http4s.{Method, Request, Uri}
import shapeless.HList

final case class HttpApiJsonParsePipe[F[_]](schema: Schema, url: String, explorer: Explorer[F]) extends Pipe[F, HList] {

  private val request = Request[F](
    Method.GET,
    Uri.unsafeFromString(s"$url")
  )

  override def interpret: Scenario[F, HList] = Scenario.eval(
    explorer.makeGetRequest[HList](request)(schema.decoder)
  )
}

