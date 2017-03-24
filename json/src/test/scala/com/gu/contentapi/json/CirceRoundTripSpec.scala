package com.gu.contentapi.json

import com.github.agourlay.cornichon.json.JsonDiff.Diff
import com.gu.contentapi.client.model.v1._
import com.gu.contentapi.json.utils.JsonHelpers._
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax._
import io.circe.parser._
import io.circe.optics.JsonPath._
import org.scalatest.{FlatSpec, Matchers}
import com.gu.fezziwig.CirceScroogeMacros.{decodeThriftEnum, decodeThriftStruct, decodeThriftUnion, encodeThriftStruct, encodeThriftUnion}
import com.gu.contentapi.json.CirceEncoders._
import com.gu.contentapi.json.CirceDecoders._
import cats.syntax.either._


class CirceRoundTripSpec extends FlatSpec with Matchers {

  it should "round-trip a TagsResponse including a sponsored tag" in {
    /*
    Unfortunately the references field of the Tag struct was accidentally
    marked as required, making the Thrift model inconsistent with the JSON one.
    We work around this problem in Concierge.
     */
    val removeReferences = root.results.each.at("references").modify { refsOpt =>
      for {
        refs: Json <- refsOpt
        nonEmptyArray <- refs.asArray.filter(_.nonEmpty)
      } yield refs
    }

    checkRoundTrip[TagsResponse]("tags-including-sponsored-tag.json", transformAfterEncode = removeReferences)
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
    checkRoundTrip[ItemResponse]("item-content.json")
  }

  it should "round-trip a Thrift Enum with a complex name" in {
    val tagTypeBefore = TagType.NewspaperBookSection
    val json = tagTypeBefore.asJson
    val tagTypeAfter = json.as[TagType].toOption.get

    tagTypeAfter should equal(tagTypeBefore)
  }

  it should "serialize an Office to an uppercase JString" in {
    Office.Uk.asJson should be(Json.fromString("UK"))
  }

  it should "be able to deserialize a Json number to a String" in {
    parse(""" "123456789" """).getOrElse(Json.Null).as[String] should be(Right("123456789"))
    parse(""" 123456789 """).getOrElse(Json.Null).as[String] should be(Right("123456789"))
    parse(""" 123456789 """).getOrElse(Json.Null).as[Long] should be(Right(123456789L))
  }

  it should "preserve timezone information in timestamps" in {
    {
      val jsonBefore = Json.fromString("2016-05-04T12:34:56.123Z")
      val capiDateTime = jsonBefore.as[CapiDateTime].toOption
      capiDateTime should be(Some(CapiDateTime(1462365296123L, "2016-05-04T12:34:56.123Z")))
      val jsonAfter = capiDateTime.map(_.asJson)
      jsonAfter should be(Some(Json.fromString("2016-05-04T12:34:56Z")))
    }
    {
      val jsonBefore = Json.fromString("2016-05-04T12:34:56.123+01:00")
      val capiDateTime = jsonBefore.as[CapiDateTime].toOption
      capiDateTime should be(Some(CapiDateTime(1462361696123L, "2016-05-04T12:34:56.123+01:00")))
      val jsonAfter = capiDateTime.map(_.asJson)
      jsonAfter should be(Some(Json.fromString("2016-05-04T12:34:56+01:00")))
    }
  }

  it should "round-trip an ItemResponse with a quiz atom" in {
    checkRoundTrip[ItemResponse]("item-content-with-atom-quiz.json")
  }

  it should "round-trip an ItemResponse with a media atom" in {
    checkRoundTrip[ItemResponse]("item-content-with-atom-media.json")
  }

  it should "round-trip an ItemResponse with an explainer atom" in {
    checkRoundTrip[ItemResponse]("item-content-with-atom-explainer.json")
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

  it should "round-trip an EntitiesResponse" in {
    checkRoundTrip[EntitiesResponse]("entities.json")
  }

  def checkRoundTrip[T : Decoder : Encoder](jsonFileName: String,
                                  transformBeforeDecode: Json => Json = identity,
                                  transformAfterEncode: Json => Json = identity) = {

    val jsons: Option[(Json, Json)] = for {
      jsonBefore <- parse(loadJson(jsonFileName)).toOption
      transformedBefore <- jsonBefore.hcursor.downField("response").success.map(c => transformBeforeDecode(c.value))
      decoded <- transformedBefore.as[T].toOption
      encoded: Json = decoded.asJson
      jsonAfter: Json = Json.fromFields(List("response" -> transformAfterEncode(encoded)))
    } yield (jsonBefore, jsonAfter)

    jsons should not be None
    jsons.foreach(j => checkDiff(j._1, j._2))
  }

  val Identical = Diff(Json.Null, Json.Null, Json.Null)
  def checkDiff(jsonBefore: Json, jsonAfter: Json) = {
    import com.github.agourlay.cornichon.json.JsonDiff.{ diff, Diff }
    val d: Diff = diff(jsonBefore, jsonAfter)

    if (d != Identical) {
      println("JSON before:")
      println(jsonBefore.spaces2)
      println("=====")
      println("JSON after:")
      println(jsonAfter.spaces2)
      println("=====")
      println("Diff:")
      println(d)
    }

    d should be(Identical)
  }
}
