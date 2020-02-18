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
  def isRegistered(chat: Chat): F[Boolean]
  def checkPossibilityToRegister(chat: Chat): F[Unit]
  def logout(chat: Chat): F[Boolean]
}

object UserService {

  private final case class Live[F[_]: Monad: Raise[*[_], BotError]](repo: UserRepository[F]) extends UserService[F] {
    override def registerUser(chat: Chat, pass: String): F[Unit] =
      repo.registerUser(pass, chat)

    override def isRegistered(chat: Chat): F[Boolean] =
      repo.isAuth(chat).verified(_ == true)(NotAuthUserError(chat))

    override def checkPossibilityToRegister(chat: Chat): F[Unit] =
      repo.isAuth(chat).verified(_ == false)(DuplicateAuth(chat)).map( _ => ())

    override def logout(chat: Chat): F[Boolean] = repo.logoutUser(chat)
  }

  def apply[F[_]: Sync: Raise[*[_], NotAuthUserError]](repository: UserRepository[F]): F[UserService[F]] = Sync[F].delay(Live[F](repository))
}
