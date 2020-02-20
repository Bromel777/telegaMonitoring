package org.encryfoundation.tg.repositories

import java.nio.charset.StandardCharsets

import canoe.models.Chat
import cats.Monad
import cats.data.OptionT
import com.google.common.primitives.Longs
import org.encryfoundation.tg.data.Errors.{BotError, DuplicateAuth, IncorrectPassword, NotAuthUserError}
import org.encryfoundation.tg.db.Database
import tofu._
import tofu.syntax.monadic._
import tofu.syntax.raise._

trait UserRepository[F[_]] {
  def registerUser(pass: String, chat: Chat): F[Unit]
  def checkPass(pass: String, chat: Chat): F[Boolean]
  def isAuth(chat: Chat): F[Boolean]
  def isRegistered(chat: Chat): F[Boolean]
  def logoutUser(chat: Chat): F[Boolean]
  def login(chat: Chat, pass: String): F[Unit]
}

object UserRepository {

  private final case class Live[F[_]: Monad: Raise[*[_], BotError]](db: Database[F]) extends UserRepository[F] {

    override def registerUser(pass: String, chat: Chat): F[Unit] = for {
      _ <- db.get(Longs.toByteArray(chat.id)).verified(_.isEmpty)(DuplicateAuth(chat))
    } yield db.put(Longs.toByteArray(chat.id), (1: Byte) +: pass.getBytes())

    override def checkPass(pass: String, chat: Chat): F[Boolean] = (for {
      res <- OptionT(db.get(Longs.toByteArray(chat.id)))
    } yield (new String(res.drop(1), StandardCharsets.UTF_8) == pass)).fold(false)(res => res)

    override def isAuth(chat: Chat): F[Boolean] = OptionT(db.get(Longs.toByteArray(chat.id)))
      .fold(false)(elem => elem != null && elem.headOption.contains(1: Byte))

    override def logoutUser(chat: Chat): F[Boolean] = (for {
      prevUserInfo <- OptionT(db.get(Longs.toByteArray(chat.id)).verified(_.nonEmpty)(NotAuthUserError(chat)))
    } yield db.put(Longs.toByteArray(chat.id), (0: Byte) +: prevUserInfo.drop(1))).fold(false)(_ => true)

    override def isRegistered(chat: Chat): F[Boolean] = OptionT(db.get(Longs.toByteArray(chat.id)))
      .fold(false)(elem => elem != null)

    override def login(chat: Chat, pass: String): F[Unit] = for {
      passCheck <- checkPass(pass, chat).verified(_ == true)(IncorrectPassword(chat))
      _ <- db.put(Longs.toByteArray(chat.id), (1: Byte) +: pass.getBytes())
    } yield ()
  }

  def apply[F[_]: Monad: Raise[*[_], BotError]](db: Database[F]): F[UserRepository[F]] = Monad[F].pure(Live[F](db))
}
