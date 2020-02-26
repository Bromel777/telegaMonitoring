package org.encryfoundation.tg.pipesParser

import canoe.api.TelegramClient
import canoe.models.Chat
import cats.mtl.MonadState
import fastparse.{Parsed, _}
import NoWhitespace._
import cats.{Applicative, ApplicativeError, Monad, ~>}
import cats.data.OptionT
import cats.effect.Sync
import org.encryfoundation.tg.pipelines.Pipe
import cats.implicits._
import cats.tagless.FunctorK
import org.encryfoundation.tg.env.BotEnv
import tofu.Raise

import scala.util.{Failure, Success}

object Parser {

  implicit def whitespace(cfg: P[_]): P[Unit] = Lexer.WS(cfg)

  type PipeAny[A[_]] = Pipe[A, Any, Any]

  def parsePipes[F[_]: Monad](source: String)
                             (implicit F: MonadState[F, BotEnv[F]],
                              err: Raise[F, Throwable]): F[Pipe[F, Nothing, Any]] = {
    def expr[_: P] = P(Expressions.pipeline[F] ~ End)
    parse(source, expr(_)) match {
      case r: Parsed.Success[Pipe[F, Nothing, Any]] => r.value.pure[F]
      case e: Parsed.Failure => err.raise(new Throwable(s"${e}"))
    }
  }
}
