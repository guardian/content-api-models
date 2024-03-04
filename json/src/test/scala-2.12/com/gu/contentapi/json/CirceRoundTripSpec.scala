package com.gu.contentapi.json

import com.gu.contentapi.client.model.v1._
import com.gu.contentapi.json.utils.JsonHelpers._
import io.circe._
import io.circe.syntax._
import io.circe.parser._
import io.circe.optics.JsonPath._
import org.scalatest.{FlatSpec, Matchers}
import com.gu.fezziwig.CirceScroogeMacros.{decodeThriftEnum}
import com.gu.fezziwig.CirceScroogeWhiteboxMacros.{thriftStructLabelledGeneric, thriftUnionLabelledGeneric}
import com.gu.contentapi.json.CirceEncoders._
import com.gu.contentapi.json.CirceDecoders._
import cats.syntax.either._
import diffson._
import diffson.lcs._
import diffson.circe._
import diffson.jsonpatch._
import diffson.jsonpatch.simplediff._

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

    checkRoundTripSimple[TagsResponse]("tags-including-sponsored-tag.json", transformAfterEncode = removeReferences)
  }

  it should "round-trip a SectionsResponse" in {
    checkRoundTripSimple[SectionsResponse]("sections.json")
  }

  it should "round-trip an EditionsResponse" in {
    checkRoundTripSimple[EditionsResponse]("editions.json")
  }

  it should "round-trip an ErrorResponse" in {
    checkRoundTripSimple[ErrorResponse]("error.json")
  }

  it should "round-trip a VideoStatsResponse" in {
    checkRoundTripSimple[VideoStatsResponse]("video-stats.json")
  }

  it should "round-trip an AtomUsageResponse" in {
    checkRoundTripSimple[AtomUsageResponse]("atom-usage.json")
  }

  it should "round-trip a content ItemResponse" in {
    checkRoundTripSimple[ItemResponse]("item-content.json")
  }

  it should "round-trip a Thrift Enum with a complex name" in {
    val tagTypeBefore = TagType.NewspaperBookSection
    val json = tagTypeBefore.asJson
    val tagTypeAfter = json.as[TagType].toOption.get

    tagTypeAfter should equal(tagTypeBefore)
  }

  it should "serialize a US Exclusive sponsorship package as a snake-case string" in {
    SponsorshipPackage.UsExclusive.asJson should be(Json.fromString("us-exclusive"))
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

  it should "parse date with no time" in {
    val jsonBefore = Json.fromString("2016-05-04")
    val capiDateTime = jsonBefore.as[CapiDateTime].toOption
    capiDateTime should be(Some(CapiDateTime(1462320000000L, "2016-05-04T00:00:00Z")))
  }

  it should "parse date with no zone" in {
    val jsonBefore = Json.fromString("2015-10-22T17:02:41")
    val capiDateTime = jsonBefore.as[CapiDateTime].toOption
    capiDateTime should be(Some(CapiDateTime(1445533361000L, "2015-10-22T17:02:41Z")))
  }

  it should "round-trip an ItemResponse with a quiz atom" in {
    checkRoundTripSimple[ItemResponse]("item-content-with-atom-quiz.json")
  }

  it should "round-trip an ItemResponse with a media atom" in {
    checkRoundTripSimple[ItemResponse]("item-content-with-atom-media.json")
  }

  it should "round-trip an ItemResponse with an explainer atom" in {
    checkRoundTripSimple[ItemResponse]("item-content-with-atom-explainer.json")
  }

  it should "round-trip an ItemResponse with blocks" in {
    checkRoundTripSimple[ItemResponse]("item-content-with-blocks.json")
  }

  it should "round-trip an ItemResponse with a crossword" in {
    checkRoundTripSimple[ItemResponse]("item-content-with-crossword.json")
  }

  it should "round-trip an ItemResponse with a membership element" in {
    checkRoundTripSimple[ItemResponse]("item-content-with-membership-element.json")
  }

  it should "round-trip an ItemResponse with packages" in {
    checkRoundTripSimple[ItemResponse]("item-content-with-package.json")
  }

  it should "round-trip an ItemResponse with rich link element" in {
    checkRoundTripSimple[ItemResponse]("item-content-with-rich-link-element.json")
  }

  it should "round-trip an ItemResponse with tweets" in {
    checkRoundTripSimple[ItemResponse]("item-content-with-tweets.json")
  }

  it should "round-trip an ItemResponse with section, edition, most-viewed, editors-picks" in {
    checkRoundTripSimple[ItemResponse]("item-section.json")
  }

  it should "round-trip an ItemResponse with tags" in {
    checkRoundTripSimple[ItemResponse]("item-tag.json")
  }

  it should "round-trip a PackagesResponse" in {
    checkRoundTripSimple[PackagesResponse]("packages.json")
  }

  it should "round-trip a SearchResponse" in {
    checkRoundTripSimple[SearchResponse]("search.json")
  }

  it should "round-trip an EntitiesResponse" in {
    checkRoundTripSimple[EntitiesResponse]("entities.json")
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

  def checkRoundTripSimple[T : Decoder : Encoder](jsonFileName: String, transformAfterEncode: Json => Json = identity) = {
    val jsonBefore: Json = parse(loadJson(jsonFileName)).toOption.get
    val extracted: Json = jsonBefore.hcursor.downField("response").success.map(c => c.value).get
    val jsons: Either[DecodingFailure, (Json, Json)] = for {
      decoded <- extracted.as[T]
      encoded: Json = decoded.asJson
      jsonAfter: Json = Json.fromFields(List("response" -> transformAfterEncode(encoded.deepDropNullValues)))
    } yield (jsonBefore, jsonAfter)

    jsons match {
      case Left(e) => {
        fail(s"Got error decoding: $e")
      }
      case Right((j1, j2)) => checkDiff(j1, j2)
    }
  }

  def checkDiff(jsonBefore: Json, jsonAfter: Json) = {
    val d = diff(jsonBefore, jsonAfter)
    d should be(JsonPatch(Nil))
    if (d != JsonPatch(Nil)) println(d)
  }
}
