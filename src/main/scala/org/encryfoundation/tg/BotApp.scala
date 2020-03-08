package org.encryfoundation.tg

import java.io.File

import canoe.api._
import cats.Applicative
import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, ExitCode, IO, IOApp, Resource, Sync, Timer}
import cats.implicits._
import com.olegpy.meow.effects._
import fs2.Stream
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.encryfoundation.tg.commands.AuthCommands._
import org.encryfoundation.tg.commands.Command
import org.encryfoundation.tg.commands.NonAuthCommands._
import org.encryfoundation.tg.config.BotConfig
import org.encryfoundation.tg.db.Database
import org.encryfoundation.tg.env.BotEnv
import org.encryfoundation.tg.pipesParser.{Expressions, Parser, ScenariousParser}
import org.encryfoundation.tg.repositories.UserRepository
import org.encryfoundation.tg.services.{AuthService, Explorer, UserService}
import org.http4s.client.blaze.BlazeClientBuilder
import retry.Sleep

import scala.concurrent.ExecutionContext.global
import scala.io.Source

object BotApp extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    Stream.eval(Slf4jLogger.create[IO]).flatMap( implicit logger =>
        Stream.resource(env[IO]).flatMap {
          case (tgClient, config, explorer, userRepo, authService, userService, commands, parsed) =>
            implicit val client = tgClient
            val bot = Bot.polling[IO]
            Stream.eval(Logger[IO].info("Bot started!")) >>
            Stream.eval(Ref.of[IO, Map[String, Boolean]](config.nodes.nodes.map(ip => ip.toString() -> false).toMap)).flatMap { map =>
              val scenarious = commands.map(_.scenario)
              bot.follow((parsed ++ scenarious): _*)
            }
        }
    ).compile.drain.as(ExitCode.Success)
  }

  def getConfig[F[_]: Applicative]: F[BotConfig] = BotConfig.loadConfig("local.conf").pure[F]

  def env[F[_]: ConcurrentEffect: Sleep: Logger: Timer] = for {
    config  <- Resource.liftF(getConfig[F])
    implicit0(tgClient: TelegramClient[F]) <- TelegramClient.global[F](config.tg.token)
    blazeClient <- BlazeClientBuilder[F](global).resource
    explorer <- Explorer[F](blazeClient, config)
    db <- Database[F](new File("./db/"))
    repo <- Resource.liftF(UserRepository[F](db))
    authService <- Resource.liftF(AuthService[F](repo))
    userService <- Resource.liftF(UserService[F](List()))
    pipesToParse <- Resource.make(new File( "./src/main/resources/pipes" ).pure[F])(_ => ().pure[F])
        .flatMap{file => Resource.liftF(Source.fromFile(file).getLines().toList.pure[F])}
    parsedScenarious <- Resource.liftF(ScenariousParser.getScenarious(tgClient, explorer)(pipesToParse))
    map <- Resource.liftF(Ref.of[F, Map[String, Boolean]](config.nodes.nodes.map(ip => ip.toString() -> false).toMap))
    commands <- Resource.pure[F, List[Command[F]]](List(
      nodeStatusMonitoring(explorer, authService),
      chainMonitoring(explorer, authService),
      startNodeMonitoring[F](explorer, config, map, authService),
      registerUser(authService, userService),
      logoutPipeline(authService, userService),
      sendInfo(userService),
      login(authService, userService),
    ))
    menu <- Resource.pure[F, Command[F]](menu(authService, commands))
  } yield {
    (tgClient, config,  explorer, repo, authService, userService, commands :+ menu, parsedScenarious)
  }
}
