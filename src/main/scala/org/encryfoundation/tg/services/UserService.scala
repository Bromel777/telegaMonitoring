package org.encryfoundation.tg.services

import canoe.models.Chat
import cats.Monad
import cats.effect.Sync
import org.encryfoundation.tg.data.Errors.{BotError, DuplicateAuth, NotAuthUserError}
import org.encryfoundation.tg.repositories.UserRepository
import tofu._
import tofu.syntax.monadic._
import tofu.syntax.raise._

trait UserService[F[_]] {

  def registerUser(chat: Chat, pass: String): F[Unit]
  def verifyUser(chat: Chat): F[Unit]
  def checkPossibilityToRegister(chat: Chat): F[Unit]
}

object UserService {

  private final case class Live[F[_]: Monad: Raise[*[_], BotError]](repo: UserRepository[F]) extends UserService[F] {
    override def registerUser(chat: Chat, pass: String): F[Unit] =
      repo.registerUser(pass, chat.id)

    override def verifyUser(chat: Chat): F[Unit] =
      repo.checkUser(chat.id).verified(_ == true)(NotAuthUserError(chat)).map(_ => ())

    override def checkPossibilityToRegister(chat: Chat): F[Unit] =
      repo.checkUser(chat.id).verified(_ == false)(DuplicateAuth(chat)).map(_ => ())
  }

  def apply[F[_]: Sync: Raise[*[_], NotAuthUserError]](repository: UserRepository[F]): F[UserService[F]] = Sync[F].delay(Live[F](repository))
}
