package org.encryfoundation.tg.pipesParser

import cats.Monad
import cats.implicits._
import cats.mtl.MonadState
import org.encryfoundation.tg.env.BotEnv
import org.encryfoundation.tg.pipesParser.Parser.parsePipes
import tofu.Raise

object ScenariousParser {

  def getScenarious[F[_]: MonadState[*[_], BotEnv[F]]: Monad: Raise[*[_], Throwable]](pipes: List[String]) =
    pipes.traverse(parsePipes[F])
}
