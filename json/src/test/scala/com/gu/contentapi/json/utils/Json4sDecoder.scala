package com.gu.contentapi.json.utils

import org.json4s.JValue
import cats.data.Xor
import io.circe.Decoder.Result
import io.circe._
import org.json4s.JsonAST._

object Json4sDecoder {
  implicit val json4sDecoder: Decoder[JValue] = new Decoder[JValue] {
    override def apply(c: HCursor): Result[JValue] = Xor.right(c.focus.fold(
      JNull,
      (b: Boolean) => JBool(b),
      (n: JsonNumber) => n.toInt.map(JInt(_)) orElse n.toLong.map(JLong(_)) getOrElse JDouble(n.toDouble),
      (s: String) => JString(s),
      (a: List[Json]) => JArray(a.flatMap(elem => apply(elem.hcursor).toOption)),
      (a: JsonObject) => JObject(a.toMap.flatMap { case (k, v) => apply(v.hcursor).toOption.map((k, _)) }.toList)
    ))
  }
}