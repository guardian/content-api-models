package com.gu.contentapi.json

import com.gu.contentapi.client.model.v1._
import com.twitter.scrooge.{ThriftEnum, ThriftStruct}
import io.circe._
import io.circe.syntax._
import com.gu.contentapi.circe.CirceScroogeMacros._
import com.gu.contentatom.thrift.atom.explainer.ExplainerAtom
import com.gu.contentatom.thrift.atom.media.MediaAtom
import com.gu.contentatom.thrift.atom.quiz.QuizAtom
import com.gu.contentatom.thrift.atom.cta.CTAAtom
import com.gu.contentatom.thrift.atom.interactive.InteractiveAtom
import com.gu.contentatom.thrift.{Atom, AtomData}
import io.circe.generic.auto._
import org.joda.time.format.ISODateTimeFormat

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
  implicit val dateTimeEncoder = genDateTimeEncoder
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
  implicit val debugEncoder = Encoder[Debug]
  implicit val atomEncoder = AtomEncoder.genAtomEncoder
  implicit val atomsEncoder = Encoder[Atoms]
  implicit val contentEncoder = Encoder[Content]
  implicit val mostViewedVideoEncoder = Encoder[MostViewedVideo]
  implicit val packageEncoder = Encoder[Package]
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

  def genDateTimeEncoder: Encoder[CapiDateTime] = Encoder.instance[CapiDateTime] { capiDateTime =>
    val dateTime = ISODateTimeFormat.dateTime().withOffsetParsed().parseDateTime(capiDateTime.iso8601)
    // We don't include millis in JSON, for backwards-compatibility
    Json.fromString(dateTime.toString(ISODateTimeFormat.dateTimeNoMillis()))
  }

  /**
    * TODO - I *will* write a pair of macros for encoding/decoding thrift union types, then delete all this stuff.
   */
  object AtomEncoder {

    def genAtomEncoder: Encoder[Atom] = Encoder.instance[Atom] { atom =>
      Json.fromFields(List(
        "data" -> Json.fromFields(List(
          atom.atomType.name.toLowerCase -> getAtomData(atom.data)
        )),
        "contentChangeDetails" -> atom.contentChangeDetails.asJson,
        "atomType" -> atom.atomType.asJson,
        "id" -> atom.id.asJson,
        "labels" -> atom.labels.asJson,
        "defaultHtml" -> atom.defaultHtml.asJson
      ))
    }

    private def getAtomData(data: AtomData): Json = {
      data match {
        case AtomData.Quiz(quiz) => quiz.asJson(Encoder[QuizAtom])
        case AtomData.Media(media) => media.asJson(Encoder[MediaAtom])
        case AtomData.Explainer(explainer) => explainer.asJson(Encoder[ExplainerAtom])
        case AtomData.Cta(cta) => cta.asJson(Encoder[CTAAtom])
        case AtomData.Interactive(interactive) => interactive.asJson(Encoder[InteractiveAtom])
        case _ => Json.Null
      }
    }
  }

}
