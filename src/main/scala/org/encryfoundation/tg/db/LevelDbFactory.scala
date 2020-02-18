package org.encryfoundation.tg.db

import cats.effect.Sync
import org.iq80.leveldb.DBFactory

import scala.util.Try

object LevelDbFactory {
  private val nativeFactory = "org.fusesource.leveldbjni.JniDBFactory"
  private val javaFactory   = "org.iq80.leveldb.impl.Iq80DBFactory"

  def factory[F[_]: Sync]: F[DBFactory] = Sync[F].delay{
    val pairs = for {
      loader      <- List(ClassLoader.getSystemClassLoader, this.getClass.getClassLoader).view
      factoryName <- List(nativeFactory, javaFactory)
      factory     <- Try(loader.loadClass(factoryName).getConstructor().newInstance().asInstanceOf[DBFactory]).toOption
    } yield factory
    pairs.head
  }
}