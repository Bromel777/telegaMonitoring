package org.encryfoundation.tg.db

import java.io.File

import cats.effect.{Resource, Sync}
import cats.implicits._
import org.iq80.leveldb.{DB, Options}

trait Database[F[_]] {
  def get(key: Array[Byte]): F[Option[Array[Byte]]]
  def put(key: Array[Byte], value: Array[Byte]): F[Unit]
}

object Database {

  final private case class Live[F[_]: Sync](db: DB) extends Database[F] {

    override def get(key: Array[Byte]): F[Option[Array[Byte]]] = Sync[F].delay {
      val res = db.get(key)
      if (res == null) Option.empty[Array[Byte]] else res.some
    }

    override def put(key: Array[Byte], value: Array[Byte]): F[Unit] = Sync[F].delay(db.put(key, value))
  }

  def apply[F[_]: Sync](dir: File): Resource[F, Database] = for {
    factory <- Resource.liftF(LevelDbFactory.factory)
    db <- Resource.make(Sync[F].delay(factory.open(dir, new Options())))(db => Sync[F].delay(db.close()))
  } yield Live(db)
}
