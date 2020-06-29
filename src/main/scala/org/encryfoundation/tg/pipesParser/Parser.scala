package org.encryfoundation.tg.pipesParser

import canoe.api.TelegramClient
import canoe.models.Chat
import cats.mtl.MonadState
import fastparse.{Parsed, _}
import NoWhitespace._
import cats.{Applicative, ApplicativeError, Monad, ~>}
import cats.data.OptionT
import cats.effect.concurrent.Ref
import cats.effect.{IO, Sync, Timer}
import org.encryfoundation.tg.pipelines.{EnvironmentPipe, Pipe}
import cats.implicits._
import cats.tagless.FunctorK
import org.encryfoundation.tg.data.Errors.BotError
import org.encryfoundation.tg.env.BotEnv
import tofu.{HasContext, Raise}

import scala.util.{Failure, Success}

object Parser {

  def parsePipes[F[_]: HasContext[*[_], BotEnv[F]]: Monad: Timer](source: String)
                                                                  (implicit err: Raise[F, Throwable]): F[EnvironmentPipe[F]] = {
    def expr[_: P] = P(Expressions.pipeline[F] ~ End)
    parse(source, expr(_)) match {
      case r: Parsed.Success[EnvironmentPipe[F]] => r.value.pure[F]
      case e: Parsed.Failure => err.raise(new Throwable(s"${e.trace(true)}"))
    }
  }
}
