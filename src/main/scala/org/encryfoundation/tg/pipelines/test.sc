import io.circe.generic.auto._
import io.circe.syntax._
import org.encryfoundation.tg.pipelines.json.{IntJsonType, Schema}

//
case class Bar(i: Int, t: Int)

val doc = Bar(13, 20).asJson

val keys = doc.hcursor.keys.get.toList

val simpleSchema = Schema(Map("i" -> IntJsonType(), "t" -> IntJsonType()))

println(simpleSchema.parse(doc))