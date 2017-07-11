package com.gu.contentapi.json

import io.circe._
import com.gu.contentatom.thrift.{Atom, AtomData, AtomType}
import com.gu.fezziwig.CirceScroogeMacros.{decodeThriftEnum, decodeThriftStruct, decodeThriftUnion}
import com.gu.contentapi.client.model.v1._
import org.joda.time.format.ISODateTimeFormat
import cats.syntax.either._
import com.gu.contententity.thrift.Entity
import com.gu.story.model.v1.Story

object CirceDecoders {

  /**
    * We override Circe's provided behaviour so we can emulate json4s's
    * "silently convert a Long to a String" behaviour.
    */
  implicit final val decodeString: Decoder[String] = new Decoder[String] {
    final def apply(c: HCursor): Decoder.Result[String] = {
      val maybeFromStringOrLong = c.value.asString.orElse(c.value.asNumber.flatMap(_.toLong.map(_.toString)))
      Either.fromOption(o = maybeFromStringOrLong, ifNone = DecodingFailure("String", c.history))
    }
  }

  /**
    * A CapiDateTime is an object with 2 fields. We currently need to support decoding from
    * a string or an object.
    */
  implicit val dateTimeDecoder: Decoder[CapiDateTime] = new Decoder[CapiDateTime] {
    final def apply(c: HCursor): Decoder.Result[CapiDateTime] = {
      val maybeResult = c.value.asObject.map { obj =>
        val map = obj.toMap
        val result = for {
          iso8601Json <- map.get("iso8601")
          iso8601 <- iso8601Json.asString
          dateTimeJson <- map.get("dateTime")
          dateTime <- dateTimeJson.asNumber.flatMap(_.toLong)
        } yield CapiDateTime.apply(dateTime, iso8601)

        Either.fromOption(result, DecodingFailure("dateTimeDecoder: invalid object", c.history))

      } orElse {
        c.value.asString.map { dateTimeString =>
          val dateTime = ISODateTimeFormat.dateOptionalTimeParser().withOffsetParsed().parseDateTime(dateTimeString)
          Either.right(CapiDateTime.apply(dateTime.getMillis, dateTime.toString(ISODateTimeFormat.dateTime())))
        }
      }

      maybeResult getOrElse Either.left(DecodingFailure("dateTimeDecoder: must be string or object", c.history))
    }
  }


  /**
    * We override Circe's provided behaviour so we can decode the JSON strings "true" and "false"
    * into their corresponding booleans.
    */
  implicit final val decodeBoolean: Decoder[Boolean] = new Decoder[Boolean] {
    final def apply(c: HCursor): Decoder.Result[Boolean] = {
      val maybeFromBooleanOrString = c.value.asBoolean.orElse(c.value.asString.flatMap {
        case "true" => Some(true)
        case "false" => Some(false)
        case _ => None
      })
      Either.fromOption(o = maybeFromBooleanOrString, ifNone = DecodingFailure("Boolean", c.history))
    }
  }

  // The following implicits technically shouldn't be necessary
  // but stuff doesn't compile without them
  implicit val contentFieldsDecoder = Decoder[ContentFields]
  implicit val editionDecoder = Decoder[Edition]
  implicit val sponsorshipDecoder = Decoder[Sponsorship]
  implicit val tagDecoder = Decoder[Tag]
  implicit val assetDecoder = Decoder[Asset]
  implicit val elementDecoder = Decoder[Element]
  implicit val referenceDecoder = Decoder[Reference]
  implicit val blockElementDecoder = Decoder[BlockElement]
  implicit val blockDecoder = Decoder[Block]
  implicit val blocksDecoder = genBlocksDecoder
  implicit val rightsDecoder = Decoder[Rights]
  implicit val crosswordEntryDecoder = genCrosswordEntryDecoder
  implicit val crosswordDecoder = Decoder[Crossword]
  implicit val contentStatsDecoder = Decoder[ContentStats]
  implicit val sectionDecoder = Decoder[Section]
  implicit val debugDecoder = Decoder[Debug]
  implicit val atomTypeDecoder = Decoder[AtomType]
  implicit val atomDataDecoder = Decoder[AtomData]
  implicit val atomDecoder = Decoder[Atom]
  implicit val atomsDecoder = Decoder[Atoms]
  implicit val contentDecoder = Decoder[Content]
  implicit val mostViewedVideoDecoder = Decoder[MostViewedVideo]
  implicit val pathAndStoryQuestionsAtomIdDecoder = Decoder[PathAndStoryQuestionsAtomId]
  implicit val networkFrontDecoder = Decoder[NetworkFront]
  implicit val packageArticleDecoder = Decoder[PackageArticle]
  implicit val packageDecoder = Decoder[Package]
  implicit val itemResponseDecoder = Decoder[ItemResponse]
  implicit val searchResponseDecoder = Decoder[SearchResponse]
  implicit val editionsResponseDecoder = Decoder[EditionsResponse]
  implicit val tagsResponseDecoder = Decoder[TagsResponse]
  implicit val sectionsResponseDecoder = Decoder[SectionsResponse]
  implicit val atomsResponseDecoder = Decoder[AtomsResponse]
  implicit val packagesResponseDecoder = Decoder[PackagesResponse]
  implicit val errorResponseDecoder = Decoder[ErrorResponse]
  implicit val videoStatsResponseDecoder = Decoder[VideoStatsResponse]
  implicit val atomsUsageResponseDecoder = Decoder[AtomUsageResponse]
  implicit val removedContentResponseDecoder = Decoder[RemovedContentResponse]
  implicit val entityDecoder = Decoder[Entity]
  implicit val entitiesResponseDecoder = Decoder[EntitiesResponse]
  implicit val ophanStoryQuestionsResponseDecoder = Decoder[OphanStoryQuestionsResponse]
  implicit val storyDecoder = Decoder[Story]
  implicit val storiesResponseDecoder = Decoder[StoriesResponse]

  // These two need to be written manually. I think the `Map[K, V]` type having 2 type params causes implicit divergence,
  // although shapeless's Lazy is supposed to work around that.

  def genBlocksDecoder(implicit blockDecoder: Decoder[Block]): Decoder[Blocks] = Decoder.instance[Blocks] { cursor =>
    for {
      main <- cursor.get[Option[Block]]("main")
      body <- cursor.get[Option[Seq[Block]]]("body")
      totalBodyBlocks <- cursor.get[Option[Int]]("totalBodyBlocks")
      requestedBodyBlocks <- cursor.get[Option[Map[String, Seq[Block]]]]("requestedBodyBlocks")
    } yield Blocks(main, body, totalBodyBlocks, requestedBodyBlocks)
  }

  def genCrosswordEntryDecoder(implicit dec: Decoder[Option[Map[String,Seq[Int]]]]): Decoder[CrosswordEntry] = Decoder.instance[CrosswordEntry] { cursor =>
    for {
      id <- cursor.get[String]("id")
      number <- cursor.get[Option[Int]]("number")
      humanNumber <- cursor.get[Option[String]]("humanNumber")
      direction <- cursor.get[Option[String]]("direction")
      position <- cursor.get[Option[CrosswordPosition]]("position")
      separatorLocations <- cursor.get[Option[Map[String, Seq[Int]]]]("separatorLocations")
      length <- cursor.get[Option[Int]]("length")
      clue <- cursor.get[Option[String]]("clue")
      group <- cursor.get[Option[Seq[String]]]("group")
      solution <- cursor.get[Option[String]]("solution")
      format <- cursor.get[Option[String]]("format")
    } yield CrosswordEntry(
      id,
      number,
      humanNumber,
      direction,
      position,
      separatorLocations,
      length,
      clue,
      group,
      solution,
      format
    )
  }

}
