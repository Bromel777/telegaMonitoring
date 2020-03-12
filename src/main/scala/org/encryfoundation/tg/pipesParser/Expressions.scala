package org.encryfoundation.tg.pipesParser

import canoe.api.TelegramClient
import cats.effect.IO
import cats.implicits._
import cats.mtl.MonadState
import cats.{Applicative, Monad}
import fastparse.SingleLineWhitespace._
import fastparse._
import org.encryfoundation.prismlang.parser.{Basic}
import org.encryfoundation.tg.env.BotEnv
import org.encryfoundation.tg.pipelines.chat.{InvokePipe, PrintPipe, ReadPipe}
import org.encryfoundation.tg.pipelines.json.Schema.Field
import org.encryfoundation.tg.pipelines.json.{HttpApiJsonParsePipe, Schema, StringJsonType}
import org.encryfoundation.tg.pipelines.{Pipe, PipeEnv}
import tofu.Raise

object Expressions {

  def STRING[_: P] = P(CharIn("a-z").rep(1).!)
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

  def httpApiJsonParsePipe[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](implicit t: P[_], f1: Raise[F, Throwable]) =
    P("ParseJson(url = " ~ URL ~ ", fields = ["~ field ~"])").map {
      case (urlToParse, fields) => HttpApiJsonParsePipe(schema = Schema(fields.map(el => el.name -> el.fType).toMap), url = urlToParse)
    }

  def apipes[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](implicit t: P[_], f1: Raise[F, Throwable]) =
    P( httpApiJsonParsePipe[F] | readPipe[F] | printPipe[F] )

  def pipes[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](implicit t: P[_], f1: Raise[F, Throwable]) = P(("=>" ~ apipes[F]).rep)

  def pipeline[F[_]: MonadState[*[_], BotEnv[F]]: Applicative](implicit t: P[_], f1: Raise[F, Throwable]) =
    P (invokePipe ~ pipes).map { case (invokePipe, pipes) =>
      pipes.foldLeft(invokePipe){
        case (pipeLine, nextPipe) =>
          pipeLine.combine(nextPipe)
      }
    }

  def parseCommand[F[_]: MonadState[*[_], BotEnv[F]]: Monad](tgClient: TelegramClient[IO])(pipeToParse: String)(implicit f1: Raise[F, Throwable]) = for {
    res <- Parser.parsePipes[F](pipeToParse)
  } yield res.commonFunc(PipeEnv.empty.copy())
}
