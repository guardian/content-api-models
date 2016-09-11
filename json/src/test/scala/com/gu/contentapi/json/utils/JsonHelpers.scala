package com.gu.contentapi.json.utils

import java.nio.charset.StandardCharsets

import com.google.common.io.Resources
import io.circe.{Decoder, Json}
import io.circe.parser._

object JsonHelpers {
  def loadJson(filename: String): String = {
    Resources.toString(Resources.getResource(s"templates/$filename"), StandardCharsets.UTF_8)
  }

  def parseJson[T: Manifest : Decoder](rawJson: String): T = {
    val json = parse(rawJson).leftMap(e => throw e).getOrElse(Json.Null)
    val response = json.cursor.downField("response").map(c => c.focus).getOrElse(Json.Null)
    response.as[T].toOption.get
  }
}
