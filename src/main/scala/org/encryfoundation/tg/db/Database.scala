package org.encryfoundation.tg.db

import java.io.File

import cats.Applicative
import cats.effect.{Resource, Sync}
import cats.implicits._
import org.iq80.leveldb.{DB, Options}

trait Database[F[_]] {
  def get(key: Array[Byte]): F[Option[Array[Byte]]]
  def put(key: Array[Byte], value: Array[Byte]): F[Unit]
}

object Database {

  final private case class Live[F[_]: Applicative](db: DB) extends Database[F] {

    override def get(key: Array[Byte]): F[Option[Array[Byte]]] = {
      val res = db.get(key)
      if (res == null) Option.empty[Array[Byte]].pure[F] else res.some.pure[F]
    }

    override def put(key: Array[Byte], value: Array[Byte]): F[Unit] = db.put(key, value).pure[F]
  }

  def apply[F[_]: Sync](dir: File): Resource[F, Database] = for {
    factory <- Resource.liftF(LevelDbFactory.factory)
    db <- Resource.make(factory.open(dir, new Options()).pure[F])(_.close().pure[F])
  } yield Live(db)
}
