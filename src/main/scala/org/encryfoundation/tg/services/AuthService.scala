package org.encryfoundation.tg.services

import canoe.models.Chat
import cats.Monad
import cats.effect.Sync
import org.encryfoundation.tg.data.Errors.{BotError, DuplicateAuth, NotAuthUserError, NotRegisteredChat}
import org.encryfoundation.tg.repositories.UserRepository
import tofu._
import tofu.syntax.monadic._
import tofu.syntax.raise._

trait AuthService[F[_]] {

  def registerUser(chat: Chat, login: String, pass: String): F[Unit]
  def isAuth(chat: Chat): F[Boolean]
  def isRegistered(chat: Chat): F[Unit]
  def checkPossibilityToRegister(chat: Chat): F[Unit]
  def logout(chat: Chat): F[Boolean]
  def login(chat: Chat, pass: String): F[String]
}

object AuthService {

  private final case class Live[F[_]: Monad: Raise[*[_], BotError]](repo: UserRepository[F]) extends AuthService[F] {
    override def registerUser(chat: Chat, login: String, pass: String): F[Unit] =
      repo.registerUser(login, pass, chat)

    override def isAuth(chat: Chat): F[Boolean] =
      repo.isAuth(chat).verified(_ == true)(NotAuthUserError(chat))

    override def checkPossibilityToRegister(chat: Chat): F[Unit] =
      repo.isRegistered(chat).verified(_ == false)(DuplicateAuth(chat)).map( _ => ())

    override def logout(chat: Chat): F[Boolean] = repo.logoutUser(chat)

    override def login(chat: Chat, pass: String): F[String] = repo.login(chat, pass)

    override def isRegistered(chat: Chat): F[Unit] =
      repo.isRegistered(chat).verified(_ == true)(NotRegisteredChat(chat)).map( _ => ())
  }

  def apply[F[_]: Sync: Raise[*[_], NotAuthUserError]](repository: UserRepository[F]): F[AuthService[F]] = Sync[F].delay(Live[F](repository))
}
