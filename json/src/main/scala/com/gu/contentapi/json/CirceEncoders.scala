package com.gu.contentapi.json

import com.gu.contentapi.client.model.v1._
import com.twitter.scrooge.ThriftEnum
import io.circe._
import io.circe.syntax._
import com.gu.fezziwig.CirceScroogeMacros.{encodeThriftStruct, encodeThriftUnion}
import com.gu.contentatom.thrift.{Atom, AtomData}
import com.gu.story.model.v1.Story
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object CirceEncoders {

  private val LowerCaseFollowedByUpperCase = """([a-z])([A-Z])""".r

  /**
    * Convert a PascalCase string to a lowercase hyphenated string
    */
  private def pascalCaseToHyphenated(s: String): String =
    LowerCaseFollowedByUpperCase.replaceAllIn(s, m => m.group(1) + "-" + m.group(2)).toLowerCase

  implicit def thriftEnumEncoder[T <: ThriftEnum]: Encoder[T] = Encoder[String].contramap(t => pascalCaseToHyphenated(t.name))

  implicit def officeEncoder[A <: Office]: Encoder[A] = Encoder[String].contramap(o => o.name.toUpperCase)

  /**
    * We need special encoders for things with Maps, probably because of implicit divergence.
    * TODO - surely there's a way to avoid doing this?!
    */
  implicit val blockMapEncoder = Encoder.instance[scala.collection.Map[String,Seq[Block]]] { m =>
    val fields = m.toList.map {
      case (k, v) => k -> Json.fromValues(v.map(_.asJson))
    }
    Json.fromFields(fields)
  }

  implicit val separatorLocationsEncoder = Encoder.instance[scala.collection.Map[String,Seq[Int]]] { s =>
    val fields = s.toList.map {
      case (k, v) => k -> Json.fromValues(v.map(_.asJson))
    }
    Json.fromFields(fields)
  }

  implicit val networkFrontEncoder = Encoder[NetworkFront]
  implicit val dateTimeEncoder = genDateTimeEncoder()
  implicit val contentFieldsEncoder = Encoder[ContentFields]
  implicit val editionEncoder = Encoder[Edition]
  implicit val sponsorshipEncoder = Encoder[Sponsorship]
  implicit val tagEncoder = Encoder[Tag]
  implicit val assetEncoder = Encoder[Asset]
  implicit val elementEncoder = Encoder[Element]
  implicit val referenceEncoder = Encoder[Reference]
  implicit val blockEncoder = Encoder[Block]
  implicit val blocksEncoder = Encoder[Blocks]
  implicit val rightsEncoder = Encoder[Rights]
  implicit val crosswordEntryEncoder = Encoder[CrosswordEntry]
  implicit val crosswordEncoder = Encoder[Crossword]
  implicit val contentStatsEncoder = Encoder[ContentStats]
  implicit val sectionEncoder = Encoder[Section]
  implicit val debugEncoder: Encoder[Debug] = Encoder.instance { d =>
    Json.fromJsonObject(
      JsonObject(
        "lastSeenByPorterAt" -> d.lastSeenByPorterAt.fold(Json.Null)(_.asJson(genDateTimeEncoder(false))),
        "revisionSeenByPorter" -> d.revisionSeenByPorter.fold(Json.Null)(_.asJson),
        "contentSource" -> d.contentSource.fold(Json.Null)(_.asJson),
        "originatingSystem" -> d.originatingSystem.fold(Json.Null)(_.asJson)
      ).filter {
        case (_, Json.Null) => false
        case _ => true
      }
    )
  }
  implicit val atomDataEncoder = Encoder[AtomData]
  implicit val atomEncoder = Encoder[Atom]
  implicit val atomsEncoder = Encoder[Atoms]
  implicit val pillarEncoder = Encoder[Pillar]
  implicit val platformKeyEncoder: KeyEncoder[Platform] = KeyEncoder.instance(p => p.name)
  implicit val platformEncoder = Encoder[Platform]
  implicit val publicationEncoder = Encoder[PublicationInstance]
  implicit val contentEncoder = Encoder[Content]
  implicit val mostViewedVideoEncoder = Encoder[MostViewedVideo]
  implicit val pathAndStoryQuestionsAtomIdEncoder = Encoder[PathAndStoryQuestionsAtomId]
  implicit val packageEncoder = Encoder[Package]
  implicit val removedContentEncoder = Encoder[RemovedContent]
  implicit val itemResponseEncoder = Encoder[ItemResponse]
  implicit val searchResponseEncoder = Encoder[SearchResponse]
  implicit val editionsResponseEncoder = Encoder[EditionsResponse]
  implicit val tagsResponseEncoder = Encoder[TagsResponse]
  implicit val sectionsResponseEncoder = Encoder[SectionsResponse]
  implicit val atomsResponseEncoder = Encoder[AtomsResponse]
  implicit val packagesResponseEncoder = Encoder[PackagesResponse]
  implicit val errorResponseEncoder = Encoder[ErrorResponse]
  implicit val videoStatsResponseEncoder = Encoder[VideoStatsResponse]
  implicit val atomsUsageResponseEncoder = Encoder[AtomUsageResponse]
  implicit val removedContentResponseEncoder = Encoder[RemovedContentResponse]
  implicit val entitiesResponseEncoder = Encoder[EntitiesResponse]
  implicit val ophanStoryQuestionsResponseEncoder = Encoder[OphanStoryQuestionsResponse]
  implicit val storyEncoder = Encoder[Story]
  implicit val storiesResponseEncoder = Encoder[StoriesResponse]
  implicit val pillarsResponseEncoder = Encoder[PillarsResponse]

  def genDateTimeEncoder(truncate: Boolean = true): Encoder[CapiDateTime] = Encoder.instance[CapiDateTime] { capiDateTime =>
    val dateTime: OffsetDateTime = OffsetDateTime.parse(capiDateTime.iso8601)
    // We don't include millis in JSON, for backwards-compatibility
    Json.fromString(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(if (truncate) dateTime.truncatedTo(ChronoUnit.SECONDS) else dateTime))
  }
}
