package com.gu.contentapi.json

import com.twitter.scrooge.{ThriftEnum, ThriftStruct}
import com.gu.crier.model.event.{v1 => crier}
import com.gu.contentapi.client.model.v1._
import com.gu.contentapi.json.utils.JsonHelpers._
import io.circe._
import io.circe.syntax._
import io.circe.parser._
import io.circe.optics.JsonPath._
import org.scalacheck.ScalacheckShapeless._
import org.scalacheck.derive.{MkArbitrary, MkShrink}
import org.scalacheck.{Arbitrary, Gen, Shrink}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.Parameters
import org.scalacheck.rng.Seed
import org.scalatest.{FlatSpec, Matchers}
import org.scalatestplus.scalacheck.{ScalaCheckPropertyChecks}
import com.gu.fezziwig.CirceScroogeMacros.{decodeThriftEnum}
import com.gu.fezziwig.CirceScroogeWhiteboxMacros.{thriftStructLabelledGeneric, thriftUnionLabelledGeneric, thriftStructGeneric}
import com.gu.contentapi.json.CirceEncoders._
import com.gu.contentapi.json.CirceDecoders._
import cats.syntax.either._
import diffson._
import diffson.lcs._
import diffson.circe._
import diffson.jsonpatch._
import diffson.jsonpatch.simplediff._
import shapeless._
import shapeless.record._
import shapeless.syntax.singleton._
import java.time.{OffsetDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter

class CirceRoundTripSpec extends FlatSpec with Matchers with ScalaCheckPropertyChecks {

  // type SectionsResponseRepr = Record.`'status -> String, 'userTier -> String, 'total -> Int, 'results -> Option[Seq[Section]]`.T
  // implicit val sectionsResponseGeneric: LabelledGeneric.Aux[SectionsResponse, SectionsResponseRepr] = thriftStructGeneric[SectionsResponse, SectionsResponseRepr]
  // implicit val sectionsResponseGenericUnlabelled: Generic.Aux[SectionsResponse, String :: String :: Int :: Option[Seq[Section]] :: HNil] = thriftStructPlainGeneric[SectionsResponse, String :: String :: Int :: Option[Seq[Section]] :: HNil]
  // implicit val arbitrarySectionsResponse: MkArbitrary[SectionsResponse] = MkArbitrary.genericProduct[SectionsResponse, String :: String :: Int :: Option[Seq[Section]] :: HNil]

  // it should "round-trip an arbitrary SectionsResponse" in {
  //   forAll { (sectionsResponse: SectionsResponse) => checkRoundTrip[SectionsResponse](sectionsResponse.asJson.noSpaces)}
  // }

  // it should "generate an arbitrary CapiDateTime" in {
  //   forAll {
  //     (cdt: CapiDateTime) => {
  //       // cdt.dateTime.toString should be (cdt.iso8601)
  //       (cdt.dateTime >= 0) should be (true)
  //     }
  //   }
  // }


  implicit override val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(100, 50, 0, 10000)
  // Disable shrinking for enums because it doesn’t really make sense
  implicit def shrinkEnum[T <: ThriftEnum]: MkShrink[T] = MkShrink.instance[T](Shrink(_ => Stream.Empty))
  // Disable shrinking for structs because it gets into an infinite loop somehow
  implicit def shrinkStruct[T <: ThriftStruct]: MkShrink[T] = MkShrink.instance[T](Shrink(_ => Stream.Empty))

  def genFixedSizeList[T](size: Int)(gen: Gen[T]): Gen[List[T]] = {
    if (size <= 0) {
      gen.map(x => List(x))
    } else {
      for {
        h <- gen
        tail <- genFixedSizeList(size - 1)(gen)
      } yield {
        h :: tail
      }
    }
  }

  implicit val arbitraryTagsResponse: Arbitrary[TagsResponse] = Arbitrary(Gen.sized(size => {
    val tag: Arbitrary[Tag] = implicitly[Arbitrary[Tag]]
    for {
      status <- arbitrary[String]
      userTier <- arbitrary[String]
      total <- Gen.choose(1, 1 + size)
      pageSize <- Gen.choose(1, total)
      pages = (total.toDouble / pageSize.toDouble).ceil.toInt
      currentPage <- Gen.choose(1, pages + 1)
      startIndex = ((currentPage - 1) * pageSize) + 1
      val tags: Gen[List[Tag]] = genFixedSizeList(pageSize)(tag.arbitrary)
      results <- tags
    } yield TagsResponse(status, userTier, total, startIndex, pageSize, currentPage, pages, results)
  }))

  implicit val arbitraryTag: Arbitrary[Tag] = Arbitrary({
    for {
      id <- arbitrary[String]
      tagType <- arbTagType.arbitrary
      sectionId <- arbitrary[Option[String]]
      sectionName <- arbitrary[Option[String]]
      webTitle <- arbitrary[String]
      webUrl <- arbitrary[String]
      apiUrl <- arbitrary[String]
      references <- Gen.listOf(implicitly[Arbitrary[Reference]].arbitrary)
      description <- arbitrary[Option[String]]
      bio <- arbitrary[Option[String]]
      bylineImageUrl <- arbitrary[Option[String]]
      bylineLargeImageUrl <- arbitrary[Option[String]]
      podcast <- arbitrary[Option[Podcast]]
      firstName <- arbitrary[Option[String]]
      lastName <- arbitrary[Option[String]]
      emailAddress <- arbitrary[Option[String]]
      twitterHandle <- arbitrary[Option[String]]
      activeSponsorships <- Gen.listOf(implicitly[Arbitrary[Sponsorship]].arbitrary).map(Some(_))
      paidContentType <- arbitrary[Option[String]]
      paidContentCampaignColour <- arbitrary[Option[String]]
      rcsId <- arbitrary[Option[String]]
      r2ContributorId <- arbitrary[Option[String]]
      tagCategories <- Gen.listOf(arbitrary[String]).map(_.toSet).map(Some(_))
      entityIds <- Gen.listOf(arbitrary[String]).map(_.toSet).map(Some(_))
      campaignInformationType <- arbitrary[Option[String]]
      internalName <- arbitrary[Option[String]]
    } yield Tag(
      id,
      tagType,
      sectionId,
      sectionName,
      webTitle,
      webUrl,
      apiUrl,
      references,
      description,
      bio,
      bylineImageUrl,
      bylineLargeImageUrl,
      podcast,
      firstName,
      lastName,
      emailAddress,
      twitterHandle,
      activeSponsorships,
      paidContentType,
      paidContentCampaignColour,
      rcsId,
      r2ContributorId,
      tagCategories,
      entityIds,
      campaignInformationType,
      internalName
    )
  })

  implicit val arbTagType: Arbitrary[TagType] = Arbitrary(Gen.oneOf(TagType.list))
  implicit val arbEventType: Arbitrary[crier.EventType] = Arbitrary(Gen.oneOf(crier.EventType.list))
  implicit val arbItemType: Arbitrary[crier.ItemType] = Arbitrary(Gen.oneOf(crier.ItemType.list))
  implicit val arbContentType: Arbitrary[ContentType] = Arbitrary(Gen.oneOf(ContentType.list))
  implicit val arbElementType: Arbitrary[ElementType] = Arbitrary(Gen.oneOf(ElementType.list))
  implicit val arbCrosswordType: Arbitrary[CrosswordType] = Arbitrary(Gen.oneOf(CrosswordType.list))
  implicit val arbAssetType: Arbitrary[AssetType] = Arbitrary(Gen.oneOf(AssetType.list))
  implicit val arbSponsorshipType: Arbitrary[SponsorshipType] = Arbitrary(Gen.oneOf(SponsorshipType.list))
  implicit val arbEmbedTracksType: Arbitrary[EmbedTracksType] = Arbitrary(Gen.oneOf(EmbedTracksType.list))
  implicit val arbPlatformType: Arbitrary[PlatformType] = Arbitrary(Gen.oneOf(PlatformType.list))
  implicit val arbListType: Arbitrary[ListType] = Arbitrary(Gen.oneOf(ListType.list))
  implicit val arbMembershipTier: Arbitrary[MembershipTier] = Arbitrary(Gen.oneOf(MembershipTier.list))
  implicit val arbOffice: Arbitrary[Office] = Arbitrary(Gen.oneOf(Office.list))
  implicit val arbCapiDateTime: Arbitrary[CapiDateTime] = Arbitrary {
    for {
      year <- Gen.choose(1900, 2100)
      month <- Gen.choose(1, 12)
      dayOfMonth <- Gen.choose(1, 28) // TODO: cover other month lengths
      hour <- Gen.choose(0, 23)
      minute <- Gen.choose(0, 59)
      second <- Gen.choose(0, 59)
      nanoOfSecond = 0 // because of rounding in API responses :(
      dateTime = OffsetDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond, ZoneOffset.UTC)
    } yield {
      CapiDateTime(dateTime.toInstant.toEpochMilli(), DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dateTime))
    }
  }

  // it should "round-trip an arbitrary ContentFields (except for truncating CapiDateTimes)" in {

  //   forAll {
  //     (cf: ContentFields) => {
  //       // println(s"Trying an arbitrary ContentFields: ${cf.asJson.spaces4}")
  //       checkReverseRoundTrip[ContentFields](cf)
  //     }
  //   }
  // }

  // it should "generate a “big” ContentFields" in {
  //   val arbContentFields: Arbitrary[ContentFields] = implicitly
  //   val parameters = Parameters.default.withSize(1000)
  //   // println(arbContentFields.arbitrary(parameters, Seed.random()).asJson.spaces4)
  // }

  // it should "round-trip an arbitrary TagsResponse" in {
  //   forAll {
  //     (tr: TagsResponse) => {
  //       // println(s"Trying an arbitrary TagsResponse: ${tr.asJson.spaces4}")
  //       checkReverseRoundTrip[TagsResponse](tr)
  //     }
  //   }
  // }

  it should "round-trip an arbitrary Sponsorship" in {
    forAll {
      (s: Sponsorship) => {
        checkReverseRoundTrip[Sponsorship](s)
      }
    }
  }

  // it should "generate a “big” TagsResponse" in {
  //   val arbTagsResponse: Arbitrary[TagsResponse] = implicitly
  //   val parameters = Parameters.default.withSize(10)
  //   println("Making a big TagsResponse:")
  //   println(arbTagsResponse.arbitrary(parameters, Seed.random()).asJson.spaces4)
  // }

  // it should "round-trip a TagsResponse including a sponsored tag" in {
  //   /*
  //   Unfortunately the references field of the Tag struct was accidentally
  //   marked as required, making the Thrift model inconsistent with the JSON one.
  //   We work around this problem in Concierge.
  //    */
  //   val removeReferences = root.results.each.at("references").modify { refsOpt =>
  //     for {
  //       refs: Json <- refsOpt
  //       nonEmptyArray <- refs.asArray.filter(_.nonEmpty)
  //     } yield refs
  //   }

  //   checkRoundTripFile[TagsResponse]("tags-including-sponsored-tag.json", transformAfterEncode = removeReferences)
  // }

  // it should "round-trip a SectionsResponse" in {
  //   checkRoundTripFile[SectionsResponse]("sections.json")
  // }

  // it should "round-trip an EditionsResponse" in {
  //   checkRoundTripFile[EditionsResponse]("editions.json")
  // }

  // it should "round-trip an ErrorResponse" in {
  //   checkRoundTripFile[ErrorResponse]("error.json")
  // }

  // it should "round-trip a VideoStatsResponse" in {
  //   checkRoundTripFile[VideoStatsResponse]("video-stats.json")
  // }

  // it should "round-trip an AtomUsageResponse" in {
  //   checkRoundTripFile[AtomUsageResponse]("atom-usage.json")
  // }

  // it should "round-trip a content ItemResponse" in {
  //   checkRoundTripFile[ItemResponse]("item-content.json")
  // }

  // it should "round-trip a Thrift Enum with a complex name" in {
  //   val tagTypeBefore = TagType.NewspaperBookSection
  //   val json = tagTypeBefore.asJson
  //   val tagTypeAfter = json.as[TagType].toOption.get

  //   tagTypeAfter should equal(tagTypeBefore)
  // }

  // it should "serialize an Office to an uppercase JString" in {
  //   Office.Uk.asJson should be(Json.fromString("UK"))
  // }

  // it should "be able to deserialize a Json number to a String" in {
  //   parse(""" "123456789" """).getOrElse(Json.Null).as[String] should be(Right("123456789"))
  //   parse(""" 123456789 """).getOrElse(Json.Null).as[String] should be(Right("123456789"))
  //   parse(""" 123456789 """).getOrElse(Json.Null).as[Long] should be(Right(123456789L))
  // }

  // it should "preserve timezone information in timestamps" in {
  //   {
  //     val jsonBefore = Json.fromString("2016-05-04T12:34:56.123Z")
  //     val capiDateTime = jsonBefore.as[CapiDateTime].toOption
  //     capiDateTime should be(Some(CapiDateTime(1462365296123L, "2016-05-04T12:34:56.123Z")))
  //     val jsonAfter = capiDateTime.map(_.asJson)
  //     jsonAfter should be(Some(Json.fromString("2016-05-04T12:34:56Z")))
  //   }
  //   {
  //     val jsonBefore = Json.fromString("2016-05-04T12:34:56.123+01:00")
  //     val capiDateTime = jsonBefore.as[CapiDateTime].toOption
  //     capiDateTime should be(Some(CapiDateTime(1462361696123L, "2016-05-04T12:34:56.123+01:00")))
  //     val jsonAfter = capiDateTime.map(_.asJson)
  //     jsonAfter should be(Some(Json.fromString("2016-05-04T12:34:56+01:00")))
  //   }
  // }

  // it should "parse date with no time" in {
  //   val jsonBefore = Json.fromString("2016-05-04")
  //   val capiDateTime = jsonBefore.as[CapiDateTime].toOption
  //   capiDateTime should be(Some(CapiDateTime(1462320000000L, "2016-05-04T00:00:00Z")))
  // }

  // it should "parse date with no zone" in {
  //   val jsonBefore = Json.fromString("2015-10-22T17:02:41")
  //   val capiDateTime = jsonBefore.as[CapiDateTime].toOption
  //   capiDateTime should be(Some(CapiDateTime(1445533361000L, "2015-10-22T17:02:41Z")))
  // }

  // it should "round-trip an ItemResponse with a quiz atom" in {
  //   checkRoundTripFile[ItemResponse]("item-content-with-atom-quiz.json")
  // }

  // it should "round-trip an ItemResponse with a media atom" in {
  //   checkRoundTripFile[ItemResponse]("item-content-with-atom-media.json")
  // }

  // it should "round-trip an ItemResponse with an explainer atom" in {
  //   checkRoundTripFile[ItemResponse]("item-content-with-atom-explainer.json")
  // }

  // it should "round-trip an ItemResponse with blocks" in {
  //   checkRoundTripFile[ItemResponse]("item-content-with-blocks.json")
  // }

  // it should "round-trip an ItemResponse with a crossword" in {
  //   checkRoundTripFile[ItemResponse]("item-content-with-crossword.json")
  // }

  // it should "round-trip an ItemResponse with a membership element" in {
  //   checkRoundTripFile[ItemResponse]("item-content-with-membership-element.json")
  // }

  // it should "round-trip an ItemResponse with packages" in {
  //   checkRoundTripFile[ItemResponse]("item-content-with-package.json")
  // }

  // it should "round-trip an ItemResponse with rich link element" in {
  //   checkRoundTripFile[ItemResponse]("item-content-with-rich-link-element.json")
  // }

  // it should "round-trip an ItemResponse with tweets" in {
  //   checkRoundTripFile[ItemResponse]("item-content-with-tweets.json")
  // }

  // it should "round-trip an ItemResponse with section, edition, most-viewed, editors-picks" in {
  //   checkRoundTripFile[ItemResponse]("item-section.json")
  // }

  // it should "round-trip an ItemResponse with tags" in {
  //   checkRoundTripFile[ItemResponse]("item-tag.json")
  // }

  // it should "round-trip a PackagesResponse" in {
  //   checkRoundTripFile[PackagesResponse]("packages.json")
  // }

  // it should "round-trip a SearchResponse" in {
  //   checkRoundTripFile[SearchResponse]("search.json")
  // }

  // it should "round-trip an EntitiesResponse" in {
  //   checkRoundTripFile[EntitiesResponse]("entities.json")
  // }

  def checkRoundTripFile[T : Decoder : Encoder](jsonFileName: String,
                                  transformBeforeDecode: Json => Json = identity,
                                  transformAfterEncode: Json => Json = identity) =
    checkRoundTrip[T](loadJson(jsonFileName), transformBeforeDecode, transformAfterEncode)

  def checkRoundTrip[T : Decoder : Encoder](json: String,
                                  transformBeforeDecode: Json => Json = identity,
                                  transformAfterEncode: Json => Json = identity) = {

    val jsons: Option[(Json, Json)] = for {
      jsonBefore <- parse(json).toOption
      transformedBefore <- jsonBefore.hcursor.downField("response").success.map(c => transformBeforeDecode(c.value))
      decoded <- transformedBefore.as[T].toOption
      encoded: Json = decoded.asJson
      jsonAfter: Json = Json.fromFields(List("response" -> transformAfterEncode(encoded)))
    } yield (jsonBefore, jsonAfter)

    jsons should not be None
    jsons.foreach(j => checkDiff(j._1, j._2))
  }

  def checkReverseRoundTrip[T : Decoder : Encoder](t: T) = {
    val encoded: Json = t.asJson
    val decoded = encoded.as[T]
    decoded should be (Right(t))
  }

  def checkDiff(jsonBefore: Json, jsonAfter: Json) = {
    val d = diff(jsonBefore, jsonAfter)
    d should be(JsonPatch(Nil))
    if (d != JsonPatch(Nil)) println(d)
  }
}
