package org.encryfoundation.tg.repositories

import java.nio.charset.StandardCharsets

import cats.data.OptionT
import cats.effect.Sync
import org.encryfoundation.tg.db.Database

trait UserRepository[F[_]] {
  def registerUser(login: String, pass: String): F[Unit]
  def checkPass(login: String, pass: String): F[Boolean]
  def checkUser(login: String): F[Boolean]
}

object UserRepository {

  private final case class Live[F[_]: Sync](db: Database[F]) extends UserRepository[F] {

    override def registerUser(login: String, pass: String): F[Unit] =
      db.put(login.getBytes(), pass.getBytes())

    override def checkPass(login: String, pass: String): F[Boolean] = (for {
      res <- OptionT(db.get(login.getBytes))
    } yield (new String(res, StandardCharsets.UTF_8) == pass)).fold(false)(res => res)

    override def checkUser(login: String): F[Boolean] = OptionT(db.get(login.getBytes)).fold(false)(_ => true)
  }
}
