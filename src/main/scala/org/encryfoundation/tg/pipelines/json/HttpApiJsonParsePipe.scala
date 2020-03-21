package org.encryfoundation.tg.pipelines.json

import canoe.api.Scenario
import canoe.api._
import canoe.models.outgoing.TextContent
import cats.Applicative
import cats.mtl.MonadState
import org.encryfoundation.tg.data.Errors.{BotError, PipeErr}
import org.encryfoundation.tg.env.BotEnv
import org.encryfoundation.tg.pipelines.{EnvironmentPipe, Pipe, PipeEnv}
import org.encryfoundation.tg.services.Explorer
import tofu._
import tofu.syntax.raise._
import tofu.syntax.monadic._
import tofu.syntax.handle._
import org.http4s.{Method, Request, Uri}
import shapeless.{HList, Lub}
import tofu.Raise

object HttpApiJsonParsePipe {

  def apply[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](schema: Schema = Schema.empty,
                                                            url: String)(implicit f1: Raise[F, Throwable]): EnvironmentPipe[F] = {
    val request = Request[F](
      Method.GET,
      Uri.unsafeFromString(s"$url")
    )

    EnvironmentPipe[F]((env: PipeEnv) =>
      (for {
        botEnv <- Scenario.eval(MonadState[F, BotEnv[F]].get)
        res <- Scenario.eval(botEnv.explorer.makeGetRequest[List[(String, Any)]](request)(schema.decoder)).handleErrorWith {
          _: Throwable => Scenario.eval(f1.raise(PipeErr(env.chat.get)))
        }
      } yield env.copy(variables = env.variables ++ res.map{ case (elemName, elemValue) =>
        elemName -> Value(elemName, elemValue.toString, StringJsonType)}
      ))
    )
  }
}
