package com.gu.contentapi.json

import com.gu.contentapi.client.model.v1.TagsResponse
import com.gu.contentapi.json.utils.JsonLoader
import org.json4s.{Diff, Extraction}
import org.json4s.JsonAST.{JArray, JNothing, JObject}
import org.json4s.jackson.JsonMethods
import org.scalatest._

class SerializationSpec extends FlatSpec with Matchers {

  implicit val formats = Serialization.formats

  it should "round-trip a TagsResponse including a sponsored tag" in {
    val jsonBefore = JsonMethods.parse(JsonLoader.loadJson("tags-including-sponsored-tag.json"))
    val deserialized = (jsonBefore \ "response").extract[TagsResponse]
    val jsonAfter = JObject("response" -> Extraction.decompose(deserialized))

    /*
    Unfortunately the references field of the Tag struct was accidentally
    marked as required, making the Thrift model inconsistent with the JSON one.
    We work around this problem in Concierge.
     */
    val jsonAfterWithReferencesRemoved = jsonAfter.removeField {
      case ("references", JArray(Nil)) => true
      case _ => false
    }

    val diff = Diff.diff(jsonBefore, jsonAfterWithReferencesRemoved)

    if (diff != Diff(JNothing, JNothing, JNothing)) {
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
  }

}
