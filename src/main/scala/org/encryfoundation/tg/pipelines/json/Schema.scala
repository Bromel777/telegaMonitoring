package org.encryfoundation.tg.pipelines.json

import io.circe.{Decoder, HCursor, Json}
import shapeless.{HList, HNil}

trait Schema {
  def parse(hCursor: HCursor): List[(String, Any)]
  val decoder: Decoder[List[(String, Any)]]
}

object Schema {

  val jsonTypes: Map[String, JsonType] = Map(
    "string" -> StringJsonType,
    "int" -> IntJsonType,
    "long" -> LongJsonType
  )

  case class Field(name: String, fType: JsonType)

  def apply[F[_]](userSchema: Map[String, JsonType]): Schema = new Schema {
    override def parse(hCursor: HCursor): List[(String, Any)] = {
      val keys = hCursor.keys.get
      keys.foldLeft(List.empty[(String, Any)]) {
        case (list, key) if userSchema.contains(key) =>
          val elem = userSchema.getOrElse(key, StringJsonType)
          (key -> hCursor.downField(key).as[elem.Underlying](elem.decoder)) :: list
        case (list, _) => list
      }
    }

    override val decoder: Decoder[List[(String, Any)]] = (c: HCursor) => Right(parse(c))
  }

  def empty = Schema.apply(Map.empty)
}
