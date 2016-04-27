package com.gu.contentapi.json

import com.gu.contentapi.client.model.v1._
import com.gu.contentapi.json.utils.JsonLoader
import org.json4s.{JValue, Diff, Extraction}
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
    val jsonAfterWithReferencesRemoved = jsonAfter.transformField {
      case ("results", JArray(results)) =>
        val withoutRefs = results.map(_.removeField {
          case ("references", JArray(Nil)) => true
          case _ => false
        })
        ("results", JArray(withoutRefs))
    }

    checkDiff(jsonBefore, jsonAfterWithReferencesRemoved)
  }

  it should "round-trip a SectionsResponse" in {
    checkRoundTrip[SectionsResponse]("sections.json")
  }

  it should "round-trip a RemovedContentResponse" in {
    checkRoundTrip[RemovedContentResponse]("removed.json")
  }

  it should "round-trip an EditionsResponse" in {
    checkRoundTrip[EditionsResponse]("editions.json")
  }

  it should "round-trip an ErrorResponse" in {
    checkRoundTrip[ErrorResponse]("error.json")
  }

  it should "round-trip a Thrift Enum with a complex name" in {
    val tagTypeBefore = TagType.NewspaperBookSection
    val json = Extraction.decompose(tagTypeBefore)
    val tagTypeAfter = json.extract[TagType]

    tagTypeAfter should equal(tagTypeBefore)
  }

  val Identical = Diff(JNothing, JNothing, JNothing)

  def checkRoundTrip[T: Manifest](jsonFileName: String) = {
    val jsonBefore = JsonMethods.parse(JsonLoader.loadJson(jsonFileName))
    val deserialized = (jsonBefore \ "response").extract[T]
    val jsonAfter = JObject("response" -> Extraction.decompose(deserialized))

    checkDiff(jsonBefore, jsonAfter)
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

}
