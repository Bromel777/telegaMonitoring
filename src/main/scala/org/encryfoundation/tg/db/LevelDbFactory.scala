package org.encryfoundation.tg.db

import cats.{Monad, MonadError}
import org.iq80.leveldb.DBFactory

import scala.util.Try

object LevelDbFactory {
  private val nativeFactory = "org.fusesource.leveldbjni.JniDBFactory"
  private val javaFactory   = "org.iq80.leveldb.impl.Iq80DBFactory"

  def factory[F[_] : MonadError[*, Throwable]]: F[DBFactory] = {
    val pairs = for {
      loader      <- List(ClassLoader.getSystemClassLoader, this.getClass.getClassLoader).view
      factoryName <- List(nativeFactory, javaFactory)
      factory     <- Try(loader.loadClass(factoryName).getConstructor().newInstance().asInstanceOf[DBFactory]).toOption
    } yield (factoryName, factory)

    pairs.headOption.fold[F[DBFactory]](
      MonadError[F, Throwable].raiseError(
        new RuntimeException(s"Could not load any of the factory classes: $nativeFactory, $javaFactory")
      )
    )(Monad[F].pure(_))
  }
}