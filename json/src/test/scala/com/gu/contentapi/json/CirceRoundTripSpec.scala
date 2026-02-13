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

    checkRoundTrip[TagsResponse]("tags-including-sponsored-tag.json", transformAfterEncode = removeReferences)
  }

  it should "round-trip a SectionsResponse" in {
    checkRoundTrip[SectionsResponse]("sections.json")
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

  it should "serialise and deserialise the renamed fields “startDate” and “endDate” as “start” and “end” respectively" in {
    checkRoundTrip[MembershipElementFields]("membership-element.json")
    checkRoundTrip[MembershipElementFields]("membership-element-no-start-date.json")
  }

  def checkRoundTrip[T : Decoder : Encoder](
    jsonFileName: String,
    transformBeforeDecode: Json => Json = identity,
    transformAfterEncode: Json => Json = identity
  ): Unit = {
    val test: Either[RoundTripCheckError, Unit] = for {
       jsonBefore <- parse(loadJson(jsonFileName)).left.map(FailedToLoadJson(jsonFileName, _))
       transformedBefore <- jsonBefore.hcursor.downField("response").success.map(
         c => transformBeforeDecode(c.value)
       ).toRight(NoResponseField(jsonFileName, jsonBefore))
       decoded <- transformedBefore.as[T].left.map(FailedToDecode(transformedBefore, _))
       encoded: Json = decoded.asJson
       jsonAfter: Json = Json.fromFields(
         List("response" -> transformAfterEncode(encoded.deepDropNullValues))
       )
     } yield {
       checkDiff(jsonBefore, jsonAfter)
     }
    test.left.map{
      case FailedToLoadJson(filename, failure) =>
        fail(s"Failed to load json from $filename for test: $failure")
      case NoResponseField(filename, json) =>
        fail(s"Expected to find a “response” field at the top level of $filename, instead got: $json")
      case FailedToDecode(transformed, failure) =>
        fail(s"Failed to decode transformed json ($transformed), got failure: $failure")
    }.merge
  }

  sealed trait RoundTripCheckError
  case class FailedToLoadJson(filename: String, parseFailure: ParsingFailure) extends RoundTripCheckError
  case class NoResponseField(filename: String, json: Json) extends RoundTripCheckError
  case class FailedToDecode(transformed: Json, failure: DecodingFailure) extends RoundTripCheckError

  def checkDiff(jsonBefore: Json, jsonAfter: Json): Unit = {
    val d = diff(jsonBefore, jsonAfter)
    d should be(JsonPatch(Nil))
    if (d != JsonPatch(Nil)) println(d)
  }
}
