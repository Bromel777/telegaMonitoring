package org.encryfoundation.tg.repositories

import java.nio.charset.StandardCharsets

import canoe.models.Chat
import cats.Monad
import cats.data.OptionT
import cats.instances.list._
import cats.syntax.traverse._
import com.google.common.primitives.Longs
import org.encryfoundation.tg.data.Errors.{BotError, DuplicateAuth, IncorrectPassword, NotAuthUserError}
import org.encryfoundation.tg.db.Database
import tofu._
import tofu.syntax.monadic._
import tofu.syntax.raise._

trait UserRepository[F[_]] {
  def registerUser(login: String, pass: String, chat: Chat): F[Unit]
  def checkPass(username: String, pass: String, chat: Chat): F[Boolean]
  def isAuth(chat: Chat): F[Boolean]
  def isRegistered(chat: Chat): F[Boolean]
  def logoutUser(chat: Chat): F[Boolean]
  def login(chat: Chat, username: String, pass: String): F[Unit]
}

object UserRepository {

  private final case class Live[F[_]: Monad: Raise[*[_], BotError]](db: Database[F]) extends UserRepository[F] {

    override def registerUser(login: String, pass: String, chat: Chat): F[Unit] = for {
      _ <- db.get(loginByChatIdKey(chat.id)).verified(_.isEmpty)(DuplicateAuth(chat))
    } yield userRawData(login, pass, chat.id).traverse { case (key, value) =>
      db.put(key, value)
    }

    override def checkPass(username: String, pass: String, chat: Chat): F[Boolean] = (for {
      _ <- OptionT(db.get(chatIdByLogin(username)).verified(_.nonEmpty)(NotAuthUserError(chat)))
      res <- OptionT(db.get(passByChatId(chat.id)))
    } yield (new String(res, StandardCharsets.UTF_8) == pass)).fold(false)(res => res)

    override def isAuth(chat: Chat): F[Boolean] = OptionT(db.get(chatIdAuthStatus(chat.id)))
      .fold(false)(elem => elem != null && elem.headOption.contains(1: Byte))

    override def logoutUser(chat: Chat): F[Boolean] = (for {
      prevUserInfo <- OptionT(db.get(chatIdAuthStatus(chat.id)).verified(_.nonEmpty)(NotAuthUserError(chat)))
    } yield db.put(chatIdAuthStatus(chat.id), Array(0: Byte))).fold(false)(_ => true)

    override def isRegistered(chat: Chat): F[Boolean] = OptionT(db.get(loginByChatIdKey(chat.id)))
      .fold(false)(elem => elem != null)

    override def login(chat: Chat, username: String, pass: String): F[Unit] = for {
      _ <- checkPass(username, pass, chat).verified(_ == true)(IncorrectPassword(chat))
      _ <- db.put(chatIdAuthStatus(chat.id), Array(1: Byte))
      _ <- db.get(loginByChatIdKey(chat.id))
    } yield ()
  }

  def apply[F[_]: Monad: Raise[*[_], BotError]](db: Database[F]): F[UserRepository[F]] = Monad[F].pure(Live[F](db))

  private def userRawData(login: String, pass: String, chatId: Long): List[(Array[Byte], Array[Byte])] = List(
    loginByChatIdKey(chatId) -> login.getBytes(),
    chatIdByLogin(login) -> Longs.toByteArray(chatId),
    passByChatId(chatId) -> pass.getBytes(),
    chatIdAuthStatus(chatId) -> Array(1: Byte)
  )

  private def loginByChatIdKey(chatId: Long): Array[Byte] = Longs.toByteArray(chatId) ++ "login".getBytes()

  private def chatIdByLogin(login: String): Array[Byte] = login.getBytes()

  private def chatIdAuthStatus(chatId: Long): Array[Byte] = Longs.toByteArray(chatId) ++ "status".getBytes()

  private def passByChatId(chatId: Long): Array[Byte] = Longs.toByteArray(chatId) ++ "pass".getBytes()
}
