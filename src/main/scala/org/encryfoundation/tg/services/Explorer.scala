package org.encryfoundation.tg.services

import java.util.concurrent.Executors

import cats.Monad
import cats.effect.{ConcurrentEffect, Resource, Sync}
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import io.circe.Decoder
import org.encryfoundation.tg.config.BotConfig
import org.encryfoundation.tg.data.InfoRoute
import org.http4s.circe.jsonOf
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.{Method, Request, Uri}
import retry.RetryDetails.{GivingUp, WillDelayAndRetry}
import retry.{RetryDetails, _}
import tofu.Raise

import scala.concurrent.ExecutionContext

trait Explorer[F[_]] {

  def getInfo: F[InfoRoute]
  def nodesStatus: F[List[(Boolean, String)]]
  def makeGetRequest[T](req: Request[F])(implicit decoder: Decoder[T]): F[T]
}

object Explorer {

  final private class LiveExplorer[F[_]: Monad: Sync: Sleep: Logger](client: Client[F], config: BotConfig) extends Explorer[F] {

    override def getInfo: F[InfoRoute] = {
      val req = Request[F](
        Method.GET,
        Uri.unsafeFromString(s"http://${config.nodes.nodes.head}/info")
      )
      makeGetRequest[InfoRoute](req)
    }

    override def nodesStatus: F[List[(Boolean, String)]] = config.nodes.nodes.traverse { nodeIp =>
      val req = Request[F](
        Method.GET,
        Uri.unsafeFromString(s"http://${nodeIp}/info")
      )
      makeGetRequest[InfoRoute](req).map(_ => true -> nodeIp.toString())
        .handleError(_ => false -> nodeIp.toString())
    }

    override def makeGetRequest[T](req: Request[F])(implicit decoder: Decoder[T]): F[T] =
      retryingOnAllErrors[T](
        policy = RetryPolicies.limitRetries[F](10),
        onError = logExplorerError
      )(client.expect[T](req.uri)(jsonOf[F, T]))

    private def logExplorerError(err: Throwable, details: RetryDetails)(implicit eraise: Raise[F, Throwable]): F[Unit] = details match {
      case WillDelayAndRetry(_, retriesSoFar: Int, _) =>
        Logger[F].error(err)(s"Failure to make get request from explorer. Qty of request: $retriesSoFar")
      case GivingUp(totalRetries: Int, _) =>
        Logger[F].error(err)(s"Impossible to get data from resource after $totalRetries requests.")
    }
  }

  private def pool[F[_]](implicit F: Sync[F]): Resource[F, ExecutionContext] = Resource(F.delay {
    val executor = Executors.newCachedThreadPool()
    val ec = ExecutionContext.fromExecutor(executor)
    (ec, F.delay(executor.shutdown()))
  })

  def apply[F[_]: ConcurrentEffect: Sleep: Logger](client: Client[F], config: BotConfig): Resource[F, Explorer[F]] = for {
    ec              <- pool
    client          <- BlazeClientBuilder[F](ec).resource
  } yield new LiveExplorer(client, config)
}
