package org.encryfoundation.tg.pipesParser

import cats._
import cats.effect.concurrent.Ref
import org.encryfoundation.tg.pipelines.chat.ReadPipe
import org.scalatest.Matchers
// import cats._
import cats.data._
// import cats.data._
import cats.implicits._
// import cats.implicits._
import cats.mtl.MonadState
// import cats.mtl.MonadState
import cats.mtl.implicits._
import cats.data._
import cats.effect.IO
import org.encryfoundation.tg.env.BotEnv
import org.encryfoundation.tg.pipelines.Pipe
import tofu._
import tofu.syntax.raise._
import tofu.syntax.monadic._
import org.scalatest.propspec.AnyPropSpec
import com.olegpy.meow.effects._

class ParserTest extends AnyPropSpec with Matchers {

  property("Simple test") {

    val pipeText = "Invoke(hello) => Read(test)"

//    val t: IO[Pipe[IO, Nothing, Any]] = for {
//      ref <- Ref[IO].of(BotEnv[IO](None, None))
//      res <- ref.runState { implicit monadState: MonadState[IO, BotEnv[IO]] =>
//        Parser.parsePipes[IO](pipeText)
//      }
//    } yield res
//
//    t.unsafeRunSync().isInstanceOf[Pipe[IO, Nothing, Any]] shouldBe true
  }

}
