package org.encryfoundation.tg.pipesParser

import canoe.api.TelegramClient
import cats.effect.IO
import cats.implicits._
import cats.mtl.MonadState
import cats.{Applicative, Monad}
import fastparse.MultiLineWhitespace._
import fastparse._
import org.encryfoundation.tg.env.BotEnv
import org.encryfoundation.tg.env.conditions.{AlertCondition, ValueCondition}
import org.encryfoundation.tg.pipelines.PipeEnv
import org.encryfoundation.tg.pipelines.alerts.SchedulerAlert
import org.encryfoundation.tg.pipelines.chat.{InvokePipe, PrintPipe, ReadPipe}
import org.encryfoundation.tg.pipelines.json.Schema.Field
import scala.concurrent.duration._
import org.encryfoundation.tg.pipelines.json.{HttpApiJsonParsePipe, Schema}
import org.encryfoundation.tg.pipesParser.Expressions.schedulerAlert
import tofu.Raise

object Expressions {

  def space[_: P]  = P( CharsWhileIn(" \r\n", 0) )
  def STRING[_: P] = P(CharIn("a-z").rep(1).!)
  def INT[_: P] = P(CharIn("1-9").rep(1).!).map(_.toInt)
  def subDirUrl[_: P] = P("/" ~ CharIn("a-z").rep(1).!)
  def acceptableLettersInUrl[_: P] = P(CharIn("a-z") | CharIn("0-9"))
  def urlNextPart[_: P] = P("." ~ acceptableLettersInUrl.rep(0).!)
  def port[_: P] = P(":" ~ CharIn("0-9").rep(1).!)
  def domain[_: P] = P("." ~ CharIn("a-z").rep(1).!)
  def urlEnd[_: P] = P(port | domain)
  def URL[_: P] = P(
    "http://" ~ acceptableLettersInUrl.rep(1).! ~ urlNextPart.rep(0).! ~ urlEnd.! ~ subDirUrl.rep(0).!
  ).map { case (el1, el2, el3, el4) => "http://" + el1 + el2 + el3 + el4}
  def fieldWithType[_: P] = P(STRING.! ~ ":" ~ STRING.!).map { case (name, fType) => Field(name, Schema.jsonTypes(fType)) }
  def field[_: P] = P(fieldWithType ~ ("," ~ fieldWithType).rep(0)).map(tup => tup._1 +: tup._2.toList)

  def printPipe[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](implicit t: P[_]) =
    P("Print(" ~ STRING ~ ")").map(toPrint => PrintPipe(toPrint))

  def readPipe[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](implicit t: P[_]) =
    P("Read(" ~ STRING ~ ")").map(varName => ReadPipe[F](varName))

  def invokePipe[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](implicit t: P[_]) =
    P("Invoke(" ~ STRING ~ ")").map(invokeName => InvokePipe(invokeName))

  def valueCondition[F[_]: Applicative](implicit t: P[_]): P[AlertCondition[F]] =
    P("ValueAlert(" ~ STRING ~ "==" ~ STRING ~ ")").map { vars => ValueCondition[F](List(vars._1, vars._2)) }

  def httpApiJsonParsePipe[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](implicit t: P[_], f1: Raise[F, Throwable]) =
    P("ParseJson(url = " ~ URL ~ ", fields = ["~ field ~"])").map {
      case (urlToParse, fields) => HttpApiJsonParsePipe(schema = Schema(fields.map(el => el.name -> el.fType).toMap), url = urlToParse)
    }

  def schedulerAlert[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](implicit t: P[_], f1: Raise[F, Throwable]) =
    P("Alert("~ space ~"fields = [" ~/ field ~ "]," ~ space ~ "grabberPipes = (" ~ grabberPipes ~ "),"
      ~ space ~ "alertCondition = " ~ valueCondition[F] ~ ","
      ~ space ~ "timeout = " ~ INT ~ ")").map { case (fields, grabbingPipes, condition, time) =>
      SchedulerAlert(
        fields.map(_.name),
        grabbingPipes.toList,
        condition,
        time.toInt.seconds
      )
    }

  def grabberPipes[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](implicit t: P[_], f1: Raise[F, Throwable]) =
    P( (httpApiJsonParsePipe[F] | printPipe[F]) ~ multipleGrabberPipes[F].rep(0) ).map {case (initPipe, pipes) => initPipe +: pipes}

  def multipleGrabberPipes[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](implicit t: P[_], f1: Raise[F, Throwable]) =
    P( "," ~ (httpApiJsonParsePipe[F] | printPipe[F]) )

  def apipes[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](implicit t: P[_], f1: Raise[F, Throwable]) =
    P( space ~ schedulerAlert[F] | httpApiJsonParsePipe[F] | readPipe[F] | printPipe[F])

  def pipes[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](implicit t: P[_], f1: Raise[F, Throwable]) = P(("=>" ~ apipes[F]))

  def pipeline[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](implicit t: P[_], f1: Raise[F, Throwable]) =
    P (invokePipe ~ pipes.rep(1)).map { case (invokePipe, pipes) =>
      pipes.foldLeft(invokePipe){
        case (pipeLine, nextPipe) =>
          pipeLine.combine(nextPipe)
      }
    }

  def parseCommand[F[_]: MonadState[*[_], BotEnv[F]]: Monad](tgClient: TelegramClient[IO])(pipeToParse: String)(implicit f1: Raise[F, Throwable]) = for {
    res <- Parser.parsePipes[F](pipeToParse)
  } yield res.commonFunc(PipeEnv.empty.copy())
}
