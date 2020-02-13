package com.github.bromel777.services

import java.util.concurrent.Executors

import cats.Monad
import cats.effect.{Resource, Sync}
import cats.implicits._
import cats.mtl.ApplicativeAsk
import com.github.bromel777.config.BotConfig
import com.github.bromel777.data.InfoRoute
import io.circe.Decoder
import org.http4s.Request
import org.http4s.circe.jsonOf
import org.http4s.client.Client

import scala.concurrent.ExecutionContext

trait Explorer[F[_]] {

  def getInfo: F[InfoRoute]
}

object Explorer {

  private def pool[F[_]](implicit F: Sync[F]): Resource[F, ExecutionContext] = Resource(F.delay {
    val executor = Executors.newCachedThreadPool()
    val ec = ExecutionContext.fromExecutor(executor)
    (ec, F.delay(executor.shutdown()))
  })

  final private class LiveExplorer[F[_]: Monad: ApplicativeAsk[*, BotConfig]](client: Client[F]) extends Explorer[F] {

    override def getInfo: F[InfoRoute] = for {
      config <- ApplicativeAsk[F, BotConfig].ask
    } yield ()

    private def makeGetRequest[F: Sync, T: Decoder](req: Request[F]) = client.expect[T](
      req.uri
    )(jsonOf[F, T])
  }

  def apply[F[_]: ApplicativeAsk[F, BotConfig]](client: Client[F]): Resource[F, Client[F]] = ???
}
