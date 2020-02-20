package org.encryfoundation.tg.services

import cats.Monad
import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.implicits._

trait UserService[F[_]] {
  def getLogin: F[String]
  def getMenu: F[List[(String, String)]]
  def updateLogin(newLogin: String): F[Unit]
  def updateMenu(newMenu: List[(String, String)]): F[Unit]
}

object UserService {

  final private case class Live[F[_]: Monad](login: Ref[F, String],
                                             menus: Ref[F, List[(String, String)]]) extends UserService[F] {

    override def getLogin: F[String] = login.get

    override def getMenu: F[List[(String, String)]] = menus.get

    override def updateLogin(newLogin: String): F[Unit] = login.set(newLogin)

    override def updateMenu(newMenu: List[(String, String)]): F[Unit] = menus.set(newMenu)
  }

  def apply[F[_]: Sync](guestMenu: List[(String, String)]): F[UserService[F]] = for {
    loginRef <- Ref.of[F, String]("Unknown alien")
    menuRef <- Ref.of[F, List[(String, String)]](guestMenu)
  } yield Live[F](loginRef, menuRef)
}
