package org.encryfoundation.tg.pipelines.json

import canoe.api.Scenario
import cats.Applicative
import cats.mtl.MonadState
import com.typesafe.scalalogging.StrictLogging
import org.encryfoundation.tg.data.Errors.PipeErr
import org.encryfoundation.tg.env.BotEnv
import org.encryfoundation.tg.pipelines.{EnvironmentPipe, PipeEnv}
import org.http4s.{Method, Request, Uri}
import tofu.Raise

object HttpApiJsonParsePipe extends StrictLogging {

  def apply[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](schema: Schema = Schema.empty,
                                                            url: String)(implicit f1: Raise[F, Throwable]): EnvironmentPipe[F] = {
    val request = Request[F](
      Method.GET,
      Uri.unsafeFromString(s"$url")
    )

    EnvironmentPipe[F]((env: PipeEnv) => {
      (for {
        botEnv <- Scenario.eval(MonadState[F, BotEnv[F]].get)
        res <- Scenario.eval(botEnv.explorer.makeGetRequest[List[(String, Any)]](request)(schema.decoder)).handleErrorWith {
          _: Throwable => Scenario.eval(f1.raise(PipeErr(env.chat.get)))
        }
      } yield {
        res.foldLeft(env) {
          case (environment, nextVar) =>
            environment.addToEnv(nextVar._1, Value(nextVar._1, nextVar._1.toString, StringJsonType))
        }
      })
  })
  }
}
