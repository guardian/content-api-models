package com.gu.contentapi.json

import com.gu.contentapi.circe.CirceScroogeMacros
import com.gu.contentapi.client.model.v1.{EditionsResponse, TagType}
import com.gu.contentapi.json.utils.JsonLoader
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax._
import io.circe.parser._
import org.json4s._
import org.json4s.JsonAST.JNothing
import org.json4s.jackson.JsonMethods
import org.scalatest.{FlatSpec, Matchers}
import com.gu.contentapi.circe.CirceScroogeMacros._
import com.gu.contentapi.json.CirceDeserialization._
import com.gu.contentapi.json.utils.Json4sDecoder._

class CirceRoundTripSpec extends FlatSpec with Matchers {

  it should "round-trip a Thrift Enum with a complex name" in {
    val tagTypeBefore = TagType.NewspaperBookSection
    val json = tagTypeBefore.asJson
    val tagTypeAfter = json.as[TagType].toOption.get

    tagTypeAfter should equal(tagTypeBefore)
  }

  it should "round-trip an EditionsResponse" in {
    checkRoundTrip[EditionsResponse]("editions.json")
  }


  val Identical = Diff(JNothing, JNothing, JNothing)

  def checkRoundTrip[T: Manifest : Decoder : Encoder](jsonFileName: String,
                                  transformBeforeDeserialize: Json => Json = identity,
                                  transformAfterSerialize: Json => Json = identity) = {

    val jsons: Option[(Json, Json)] = for {
      jsonBefore <- parse(JsonLoader.loadJson(jsonFileName)).toOption
      transformedBefore <- jsonBefore.cursor.downField("response").map(c => transformBeforeDeserialize(c.focus))
      deserialized <- transformedBefore.as[T].toOption
      serialized: Json = deserialized.asJson
      jsonAfter: Json = Json.fromFields(List("response" -> transformAfterSerialize(serialized)))
    } yield (jsonBefore, jsonAfter)

    jsons should not be None
    jsons.foreach(j => checkDiff(jsonToJValue(j._1).get, jsonToJValue(j._2).get))
  }

  def checkDiff(jsonBefore: JValue, jsonAfter: JValue) = {
    val diff = Diff.diff(jsonBefore, jsonAfter)

    if (diff != Identical) {
      println("JSON before:")
      println(JsonMethods.pretty(jsonBefore))
      println("=====")
      println("JSON after:")
      println(JsonMethods.pretty(jsonAfter))
      println("=====")
      println("Lost during roundtrip:")
      println(JsonMethods.pretty(diff.deleted))
      println("=====")
      println("Added by roundtrip:")
      println(JsonMethods.pretty(diff.added))
      println("=====")
      println("Changed during roundtrip")
      println(JsonMethods.pretty(diff.changed))
      println("=====")
    }

    diff should be(Identical)
  }

  def jsonToJValue(json: Json): Option[JValue] = {
    json.as[JValue].toOption
  }
}