package org.encryfoundation.tg.repositories

trait UserRepository[F[_]] {
  def registerUser(login: String, pass: String): F[Unit]
  def checkPass(login: String, pass: String): F[Boolean]
  def checkUser(login: String): F[Boolean]
}

object UserRepository {

  private final case class Live[F[_]]() extends UserRepository[F] {

    override def registerUser(login: String, pass: String): F[Unit] = ???

    override def checkPass(login: String, pass: String): F[Boolean] = ???

    override def checkUser(login: String): F[Boolean] = ???
  }
}
