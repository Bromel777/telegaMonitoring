package org.encryfoundation.tg.pipesParser

import canoe.api.{Scenario, TelegramClient}
import cats.effect.Sync
import cats.effect.concurrent.Ref
import org.encryfoundation.tg.env.BotEnv
import com.olegpy.meow.effects._
import cats.implicits._
import org.encryfoundation.tg.pipelines.PipeEnv
import org.encryfoundation.tg.pipesParser.Parser.parsePipes
import org.encryfoundation.tg.services.Explorer

object ScenariousParser {

  def getScenarious[F[_]: Sync](tgClient: TelegramClient[F],
                                explorer: Explorer[F])(pipes: List[String]) = for {
    ref <- Ref[F].of(BotEnv[F](Some(tgClient), explorer))
    res <- ref.runState { implicit monadState =>
      pipes.traverse(parsePipes[F])
    }
  } yield res.map(_.commonFunc(PipeEnv.empty).asInstanceOf[Scenario[F, Unit]])
}
