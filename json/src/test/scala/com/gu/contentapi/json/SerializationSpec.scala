package com.gu.contentapi.json

import com.gu.contentapi.client.model.v1._
import com.gu.contentapi.json.utils.JsonLoader
import org.json4s.{JValue, Diff, Extraction}
import org.json4s.JsonAST.{JString, JArray, JNothing, JObject}
import org.json4s.jackson.JsonMethods
import org.scalatest._

class SerializationSpec extends FlatSpec with Matchers {

  implicit val formats = Serialization.formats

  it should "round-trip a TagsResponse including a sponsored tag" in {
    /*
    Unfortunately the references field of the Tag struct was accidentally
    marked as required, making the Thrift model inconsistent with the JSON one.
    We work around this problem in Concierge.
     */
    val removeReferences = (json: JValue) => json.transformField {
      case ("results", JArray(results)) =>
        val withoutRefs = results.map(_.removeField {
          case ("references", JArray(Nil)) => true
          case _ => false
        })
        ("results", JArray(withoutRefs))
    }

    checkRoundTrip[TagsResponse]("tags-including-sponsored-tag.json", transformAfterSerialize = removeReferences)
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

  it should "round-trip a VideoStatsResponse" in {
    checkRoundTrip[VideoStatsResponse]("video-stats.json")
  }

  it should "round-trip an AtomUsageResponse" in {
    checkRoundTrip[AtomUsageResponse]("atom-usage.json")
  }

  it should "round-trip a content ItemResponse" in {
    checkRoundTrip[ItemResponse]("item-content.json",
      transformBeforeDeserialize = _.transformField(Serialization.destringifyFields),
      transformAfterSerialize = _.transformField {
        case ("content", json) => ("content", Serialization.stringifyContent(json))
      }
    )
  }

  it should "round-trip a Thrift Enum with a complex name" in {
    val tagTypeBefore = TagType.NewspaperBookSection
    val json = Extraction.decompose(tagTypeBefore)
    val tagTypeAfter = json.extract[TagType]

    tagTypeAfter should equal(tagTypeBefore)
  }

  it should "serialize an Office to an uppercase JString" in {
    Extraction.decompose(Office.Uk) should be(JString("UK"))
  }

  it should "preserve timezone information in timestamps" in {
    {
      val jsonBefore = JString("2016-05-04T12:34:56.123Z")
      val capiDateTime = jsonBefore.extract[CapiDateTime]
      capiDateTime should be(CapiDateTime(1462365296123L, "2016-05-04T12:34:56.123Z"))
      val jsonAfter = Extraction.decompose(capiDateTime)
      jsonAfter should be(JString("2016-05-04T12:34:56Z")) // no millis
    }
    {
      val jsonBefore = JString("2016-05-04T12:34:56.123+01:00")
      val capiDateTime = jsonBefore.extract[CapiDateTime]
      capiDateTime should be(CapiDateTime(1462361696123L, "2016-05-04T12:34:56.123+01:00"))
      val jsonAfter = Extraction.decompose(capiDateTime)
      jsonAfter should be(JString("2016-05-04T12:34:56+01:00")) // no millis
    }
  }

  it should "round-trip an ItemResponse with a quiz atom" in {
    checkRoundTrip[ItemResponse]("item-content-with-atom-quiz.json")
  }

  it should "round-trip an ItemResponse with a media atom" in {
    checkRoundTrip[ItemResponse]("item-content-with-atom-media.json")
  }

  it should "round-trip an ItemResponse with blocks" in {
    checkRoundTrip[ItemResponse]("item-content-with-blocks.json")
  }

  it should "round-trip an ItemResponse with a crossword" in {
    checkRoundTrip[ItemResponse]("item-content-with-crossword.json")
  }

  it should "round-trip an ItemResponse with a membership element" in {
    checkRoundTrip[ItemResponse]("item-content-with-membership-element.json")
  }

  it should "round-trip an ItemResponse with packages" in {
    checkRoundTrip[ItemResponse]("item-content-with-package.json")
  }

  it should "round-trip an ItemResponse with rich link element" in {
    checkRoundTrip[ItemResponse]("item-content-with-rich-link-element.json")
  }

  it should "round-trip an ItemResponse with tweets" in {
    checkRoundTrip[ItemResponse]("item-content-with-tweets.json")
  }

  it should "round-trip an ItemResponse with section, edition, most-viewed, editors-picks" in {
    checkRoundTrip[ItemResponse]("item-section.json")
  }

  it should "round-trip an ItemResponse with tags" in {
    checkRoundTrip[ItemResponse]("item-tag.json")
  }

  it should "round-trip a PackagesResponse" in {
    checkRoundTrip[PackagesResponse]("packages.json")
  }

  it should "round-trip a SearchResponse" in {
    checkRoundTrip[SearchResponse]("search.json")
  }

  val Identical = Diff(JNothing, JNothing, JNothing)

  def checkRoundTrip[T: Manifest](jsonFileName: String,
                                  transformBeforeDeserialize: JValue => JValue = identity,
                                  transformAfterSerialize: JValue => JValue = identity) = {
    val jsonBefore = JsonMethods.parse(JsonLoader.loadJson(jsonFileName))
    val deserialized = transformBeforeDeserialize(jsonBefore \ "response").extract[T]
    val serialized = Extraction.decompose(deserialized)
    val jsonAfter = JObject("response" -> transformAfterSerialize(serialized))

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
