package org.encryfoundation.tg.pipelines.json

import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._

sealed trait JsonType {
  type Underlying
  val encoder: Encoder[Underlying]
  val decoder: Decoder[Underlying]
}

object IntJsonType extends JsonType {
  override type Underlying = Int
  override val encoder: Encoder[Int] = implicitly[Encoder[Int]]
  override val decoder: Decoder[Int] = implicitly[Decoder[Int]]
}

object LongJsonType extends JsonType {
  override type Underlying = Long
  override val encoder: Encoder[Long] = implicitly[Encoder[Long]]
  override val decoder: Decoder[Long] = implicitly[Decoder[Long]]
}

object StringJsonType extends JsonType {
  override type Underlying = String
  override val encoder: Encoder[String] = implicitly[Encoder[String]]
  override val decoder: Decoder[String] = implicitly[Decoder[String]]
}
