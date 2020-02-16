package org.encryfoundation.tg

import canoe.api._
import canoe.syntax._
import cats.Applicative
import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, ExitCode, IO, IOApp, Resource}
import cats.implicits._
import fs2.Stream
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.encryfoundation.tg.config.BotConfig
import org.encryfoundation.tg.services.Explorer
import org.http4s.client.blaze.BlazeClientBuilder
import retry.Sleep

import scala.concurrent.ExecutionContext.global

object BotApp extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    Stream.eval(Slf4jLogger.create[IO]).flatMap( implicit logger =>
        Stream.resource(env[IO]).flatMap {
          case (tgClient, config, explorer) =>
            implicit val client = tgClient
            val bot = Bot.polling[IO]
            Stream.eval(Ref.of[IO, Map[String, Boolean]](config.nodes.nodes.map(ip => ip.toString() -> true).toMap)).flatMap { map =>
              bot.follow(
                scenarios.nodeStatusMonitoring(explorer, config),
                scenarios.chainMonitoring(explorer, config),
                scenarios.startNodeMonitoring(explorer, config, map).stopOn(command("cancel").isDefinedAt)
              )
            }
        }
    ).compile.drain.as(ExitCode.Success)
  }

  def getConfig[F[_]: Applicative]: F[BotConfig] = BotConfig.loadConfig("local.conf").pure[F]

  def env[F[_]: ConcurrentEffect: Sleep: Logger] = for {
    config  <- Resource.liftF(getConfig[F])
    tgClient <- TelegramClient.global[F](config.tg.token)
    blazeClient <- BlazeClientBuilder[F](global).resource
    explorer <- Explorer[F](blazeClient, config)
  } yield {
    (tgClient, config,  explorer)
  }
}
