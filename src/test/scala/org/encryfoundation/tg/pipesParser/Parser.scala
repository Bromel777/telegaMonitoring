package org.encryfoundation.tg.pipesParser

import canoe.api.TelegramClient
import canoe.models.Chat
import cats.mtl.MonadState
import fastparse.{Parsed, _}
import fastparse.MultiLineWhitespace._
import cats.{Applicative, ApplicativeError, Monad, ~>}
import cats.data.OptionT
import cats.effect.concurrent.Ref
import cats.effect.{IO, Sync, Timer}
import org.encryfoundation.tg.pipelines.{EnvironmentPipe, Pipe}
import cats.implicits._
import cats.tagless.FunctorK
import org.encryfoundation.tg.data.Errors.BotError
import org.encryfoundation.tg.env.BotEnv
import tofu.Raise

import scala.util.{Failure, Success}
import scala.util.{Failure, Success, Try}

trait Parser {

  def parsePipes[F[_]: Monad: Timer](source: String)
                                    (implicit F: MonadState[F, BotEnv[F]],
                                     err: Raise[F, Throwable]): F[Parsed[EnvironmentPipe[F]]] = {
    def expr[_: P] = P(Expressions.apipes[F] ~ End)
    parse(source, expr(_)).pure[F]
  }

}
