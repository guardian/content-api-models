package com.gu.contentapi.json

import io.circe._
import cats.data.Xor
import com.gu.contentatom.thrift.{Atom, AtomData, AtomType, ContentChangeDetails}
import com.gu.contentatom.thrift.atom.media.MediaAtom
import com.gu.contentatom.thrift.atom.quiz.QuizAtom
import com.gu.contentatom.thrift.atom.explainer.ExplainerAtom
import com.gu.contentatom.thrift.atom.cta.CTAAtom
import com.gu.contentapi.circe.CirceScroogeMacros._
import com.gu.contentapi.client.model.v1._
import org.joda.time.format.ISODateTimeFormat
import org.json4s.JValue

object CirceSerialization {

  /**
    * Encoder to convert from a json4s JValue to a Circe Json
    */
  implicit val jvalueEncoder: Encoder[JValue] = new Encoder[JValue] {
    import org.json4s.JsonAST._

    override def apply(j: JValue): Json = j match {
      case JBool(value) => Json.fromBoolean(value)
      case JString(value) => Json.fromString(value)
      case JInt(value) => Json.fromBigInt(value)
      case JLong(value) => Json.fromLong(value)
      case JDouble(value) => Json.fromDoubleOrNull(value)
      case JDecimal(value) => Json.fromBigDecimal(value)
      case JArray(elems) => Json.fromValues(elems.map(apply))
      case JObject(fields) => Json.fromFields(fields.map { case (key, value) => (key, apply(value)) })
      case JNull => Json.Null
      case JNothing => Json.Null
    }
  }

  /**
    * We override Circe's provided behaviour so we can emulate json4s's
    * "silently convert a Long to a String" behaviour.
    */
  implicit final val decodeString: Decoder[String] = new Decoder[String] {
    final def apply(c: HCursor): Decoder.Result[String] = {
      val focus = c.focus
      val fromStringOrLong = focus.asString.orElse(focus.asNumber.flatMap(_.toLong.map(_.toString)))
      Xor.fromOption(fromStringOrLong, ifNone = DecodingFailure("String", c.history))
    }
  }

  implicit val dateTimeDecoder = Decoder[String].map { dateTimeString =>
    val dateTime = ISODateTimeFormat.dateOptionalTimeParser().withOffsetParsed().parseDateTime(dateTimeString)
    CapiDateTime.apply(dateTime.getMillis, dateTime.toString(ISODateTimeFormat.dateTime()))
  }

  /**
    * We override Circe's provided behaviour so we can decode the JSON strings "true" and "false"
    * into their corresponding booleans.
    */
  implicit final val decodeBoolean: Decoder[Boolean] = new Decoder[Boolean] {
    final def apply(c: HCursor): Decoder.Result[Boolean] = {
      val focus = c.focus
      val fromBooleanOrString = focus.asBoolean.orElse(focus.asString.flatMap {
        case "true" => Some(true)
        case "false" => Some(false)
        case _ => None
      })
      Xor.fromOption(fromBooleanOrString, ifNone = DecodingFailure("Boolean", c.history))
    }
  }

  implicit val atomDecoder: Decoder[Atom] = Decoder.instance(AtomDeserialization.getAtom)
  implicit val atomsDecoder: Decoder[Atoms] = Decoder.instance(AtomDeserialization.getAtoms)

  // The following implicits technically shouldn't be necessary
  // but stuff doesn't compile without them
  implicit val contentFieldsDecoder = Decoder[ContentFields]
  implicit val editionDecoder = Decoder[Edition]
  implicit val sponsorshipDecoder = Decoder[Sponsorship]
  implicit val tagDecoder = Decoder[Tag]
  implicit val assetDecoder = Decoder[Asset]
  implicit val elementDecoder = Decoder[Element]
  implicit val referenceDecoder = Decoder[Reference]
  implicit val blockDecoder = Decoder[Block]
  implicit val blocksDecoder = genBlocksDecoder
  implicit val rightsDecoder = Decoder[Rights]
  implicit val crosswordEntryDecoder = genCrosswordEntryDecoder
  implicit val crosswordDecoder = Decoder[Crossword]
  implicit val contentStatsDecoder = Decoder[ContentStats]
  implicit val sectionDecoder = Decoder[Section]
  implicit val debugDecoder = Decoder[Debug]
  implicit val contentDecoder = Decoder[Content]
  implicit val mostViewedVideoDecoder = Decoder[MostViewedVideo]
  implicit val networkFrontDecoder = Decoder[NetworkFront]
  implicit val packageDecoder = Decoder[Package]

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

  object AtomDeserialization {

    implicit val decodeUnknownOpt: Decoder[AtomData.UnknownUnionField] =
      Decoder.instance(c =>
        Xor.left(DecodingFailure("AtomData.UnknownUnionField", c.history))
      )

    def getAtom(c: HCursor): Decoder.Result[Atom] = {
      for {
        atomType <- c.get[AtomType]("atomType")
        atomData <- getAtomData(c, atomType)
        atom <- getAtom(c, atomData)
      } yield atom
    }

    def getAtoms(c: HCursor): Decoder.Result[Atoms] = {
      for {
        quizzes <- getAtoms(c, AtomType.Quiz)
        media <- getAtoms(c, AtomType.Media)
        explainers <- getAtoms(c, AtomType.Explainer)
        cta <- getAtoms(c, AtomType.Cta)
      } yield {
        Atoms(quizzes = quizzes, media = media, explainers = explainers, cta = cta)
      }
    }

    private def getAtom(c: HCursor, atomData: AtomData): Decoder.Result[Atom] = {
      for {
        id <- c.get[String]("id")
        atomType <- c.get[AtomType]("atomType")
        labels <- c.get[Seq[String]]("labels")
        defaultHtml <- c.get[String]("defaultHtml")
        change <- c.get[ContentChangeDetails]("contentChangeDetails")
      } yield {
        Atom(id, atomType, labels, defaultHtml, atomData, change)
      }
    }

    private def getAtomData(c: HCursor, atomType: AtomType): Decoder.Result[AtomData] = {
      atomType match {
        case AtomType.Quiz => c.downField("data").get[QuizAtom]("quiz").map(json => AtomData.Quiz(json))
        case AtomType.Media => c.downField("data").get[MediaAtom]("media").map(json => AtomData.Media(json))
        case AtomType.Explainer => c.downField("data").get[ExplainerAtom]("explainer").map(json => AtomData.Explainer(json))
        case AtomType.Cta => c.downField("data").get[CTAAtom]("cta").map(json => AtomData.Cta(json))
        case _ => Xor.left(DecodingFailure("AtomData", c.history))
      }
    }

    private def getAtomTypeFieldName(atomType: AtomType): Option[String] = {
      atomType match {
        case AtomType.Quiz => Some("quizzes")
        case AtomType.Media => Some("media")
        case AtomType.Explainer => Some("explainers")
        case AtomType.Cta => Some("cta")
        case _ => None
      }
    }

    private def getAtoms(c: HCursor, atomType: AtomType): Decoder.Result[Option[Seq[Atom]]] = {
      getAtomTypeFieldName(atomType) match {
        case Some(fieldName) => c.get[Option[Seq[Atom]]](fieldName)
        case None => Xor.right(None) //ignore unrecognised atom types
      }
    }
  }
}
