package org.encryfoundation.tg.pipelines.json

import io.circe.{Decoder, HCursor, Json}
import shapeless.{HList, HNil}

trait Schema {
  def parse(hCursor: HCursor): HList
  val decoder: Decoder[HList]
}

object Schema {

  def apply(userSchema: Map[String, JsonType]): Schema = new Schema {
    override def parse(hCursor: HCursor): HList = {
      val keys = hCursor.keys.get
      keys.foldLeft(HNil: HList) {
        case (list, key) =>
          val elem = userSchema(key)
          println(key)
          println(elem)
          println(hCursor.downField(key))
          HList.unsafePrepend(
            hCursor.downField(key).as[elem.Underlying](elem.decoder).right.get :: HNil,
            list
          )
      }
    }

    override val decoder: Decoder[HList] = (c: HCursor) => Right(parse(c))
  }
}
