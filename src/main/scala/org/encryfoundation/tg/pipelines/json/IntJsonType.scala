package org.encryfoundation.tg.pipelines.json

import io.circe.{Decoder, Encoder}
import monocle.Iso

sealed trait JsonType {
  type Underlying
  def iso2String(value: String): Iso[Underlying, String]
  val encoder: Encoder[Underlying]
  val decoder: Decoder[Underlying]
}

object IntJsonType extends JsonType {
  override type Underlying = Int
  override val encoder: Encoder[Int] = implicitly[Encoder[Int]]
  override val decoder: Decoder[Int] = implicitly[Decoder[Int]]
  override def iso2String(value: String): Iso[Int, String] = Iso[Int, String](_.toString)(_.toInt)
}

object LongJsonType extends JsonType {
  override type Underlying = Long
  override val encoder: Encoder[Long] = implicitly[Encoder[Long]]
  override val decoder: Decoder[Long] = implicitly[Decoder[Long]]
  override def iso2String(value: String): Iso[Long, String] = Iso[Long, String](_.toString)(_.toLong)
}

object StringJsonType extends JsonType {
  override type Underlying = String
  override val encoder: Encoder[String] = implicitly[Encoder[String]]
  override val decoder: Decoder[String] = implicitly[Decoder[String]]
  override def iso2String(value: String): Iso[String, String] = Iso[String, String](el => el)(el => el)
}
