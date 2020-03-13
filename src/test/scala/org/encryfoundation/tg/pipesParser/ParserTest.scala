package org.encryfoundation.tg.pipesParser

import cats.Monad
import cats.effect.concurrent.Ref
import cats.kernel.Monoid
import io.circe.Decoder
import org.encryfoundation.tg.data.InfoRoute
import org.encryfoundation.tg.services.Explorer
import org.http4s.Request
import org.scalatest.Matchers
import cats._
import cats.data._
import cats.implicits._
import cats.mtl.MonadState
import cats.effect.IO
import com.olegpy.meow.effects._
import fastparse._
import org.encryfoundation.tg.env.BotEnv
import org.encryfoundation.tg.pipelines.Pipe
import org.scalatest.propspec.AnyPropSpec
import tofu.syntax.monadic._

class ParserTest extends AnyPropSpec with Matchers with Parser {

  def explorer[F[_]: Monad]: Explorer[F] = new Explorer[F] {
    override def getInfo: F[InfoRoute] = Monad[F].pure(InfoRoute(
      "", "", 0, 0, "", "", "", 0L, 0L, "", false, 0, List.empty, "", false
    ))

    override def nodesStatus: F[List[(Boolean, String)]] = Monad[F].pure(List.empty)

    override def makeGetRequest[T](req: Request[F])(implicit decoder: Decoder[T]): F[T] = ???
  }

  property("Correct parsing of alert pipe") {

    val source =
      """
        |Alert(
        | fields = [name: string, name: string],
        | grabberPipes = (
        |   ParseJson(url = http://172.16.11.12:9051/info, fields = [height:string]),
        |   ParseJson(url = http://172.16.11.12:9051/info, fields = [height:string])
        |   ),
        | alertCondition = ValueAlert(name == name),
        | timeout = 2
        |)
      """.stripMargin

    val s = for {
      ref <- Ref[IO].of(BotEnv[IO](None, explorer))
      res <- ref.runState { implicit monadState: MonadState[IO, BotEnv[IO]] =>
        parsePipes[IO](source)
      }
    } yield res

    s.unsafeRunSync().isSuccess shouldBe true
  }

}
