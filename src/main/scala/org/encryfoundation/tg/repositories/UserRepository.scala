package org.encryfoundation.tg.repositories

import java.nio.charset.StandardCharsets

import cats.data.OptionT
import cats.effect.Sync
import com.google.common.primitives.Longs
import org.encryfoundation.tg.db.Database

trait UserRepository[F[_]] {
  def registerUser(pass: String, chatId: Long): F[Unit]
  def checkPass(pass: String, chatId: Long): F[Boolean]
  def checkUser(chatId: Long): F[Boolean]
}

object UserRepository {

  private final case class Live[F[_]: Sync](db: Database[F]) extends UserRepository[F] {

    override def registerUser(pass: String, chatId: Long): F[Unit] =
      db.put(Longs.toByteArray(chatId), pass.getBytes())

    override def checkPass(pass: String, chatId: Long): F[Boolean] = (for {
      res <- OptionT(db.get(Longs.toByteArray(chatId)))
    } yield (new String(res, StandardCharsets.UTF_8) == pass)).fold(false)(res => res)

    override def checkUser(chatId: Long): F[Boolean] = OptionT(db.get(Longs.toByteArray(chatId)))
      .fold(false)(elem => elem != null)
  }

  def apply[F[_]: Sync](db: Database[F]): F[UserRepository[F]] = Sync[F].delay(Live[F](db))
}
