package org.encryfoundation.tg.pipesParser

import fastparse._
import NoWhitespace._
import org.encryfoundation.tg.pipelines.Pipes

object Lexer {

  implicit def whitespace(cfg: P[_]): P[Unit] = Lexer.WS(cfg)
  def WSChars[_: P]: P[Unit] = P(CharsWhileIn("\u0020\u0009").rep)
  def WS[_: P]: P[Unit] = P( NoCut(NoTrace(WSChars)))

  val keywords: Set[String] = Set (
    "=>"
  )
}
