package com.gu.contentapi.json

import com.gu.contentapi.client.model.v1._
import com.twitter.scrooge.{ThriftEnum, ThriftStruct}
import org.joda.time.format.ISODateTimeFormat
import org.json4s._
import org.json4s.JsonAST.JValue
import com.gu.storypackage.model.v1.{ArticleType, Group}
import com.gu.contentatom.thrift._
import com.gu.contentatom.thrift.atom.quiz._
import com.gu.contentatom.thrift.atom.media.{MediaAtom, Platform, AssetType => MediaAtomAssetType, Category}
import com.gu.contentatom.thrift.atom.explainer.{DisplayType, ExplainerAtom}

import scala.PartialFunction._
import scala.reflect.ClassTag

object Serialization {

  implicit val formats = DefaultFormats +
    AtomsSerializer +
    AtomSerializer +
    AtomTypeSerializer +
    ContentTypeSerializer +
    DateTimeSerializer +
    MembershipTierSerializer +
    OfficeSerializer +
    AssetTypeSerializer +
    ElementTypeSerializer +
    TagTypeSerializer +
    SponsorshipTypeSerializer +
    CrosswordTypeSerializer +
    StoryPackageArticleTypeSerializer +
    StoryPackageGroupSerializer +
    RemovePassthroughFieldsFromThriftStruct +
    MediaAtomAssetTypeSerializer +
    PlatformTypeSerializer +
    CategorySerializer +
    DisplayTypeSerializer

  def destringifyFields: PartialFunction[JField, JField] = {
    case JField("summary", JString(s)) => JField("summary", JBool(s.toBoolean))
    case JField("keyEvent", JString(s)) => JField("keyEvent", JBool(s.toBoolean))
    case JField("pinned", JString(s)) => JField("pinned", JBool(s.toBoolean))
    case JField("showInRelatedContent", JString(s)) => JField("showInRelatedContent", JBool(s.toBoolean))
    case JField("shouldHideAdverts", JString(s)) => JField("shouldHideAdverts", JBool(s.toBoolean))
    case JField("hasStoryPackage", JString(s)) => JField("hasStoryPackage", JBool(s.toBoolean))
    case JField("isExpired", JString(s)) => JField("isExpired", JBool(s.toBoolean))
    case JField("syndicatable", JString(s)) => JField("syndicatable", JBool(s.toBoolean))
    case JField("subscriptionDatabases", JString(s)) => JField("subscriptionDatabases", JBool(s.toBoolean))
    case JField("developerCommunity", JString(s)) => JField("developerCommunity", JBool(s.toBoolean))
    case JField("commentable", JString(s)) => JField("commentable", JBool(s.toBoolean))
    case JField("liveBloggingNow", JString(s)) => JField("liveBloggingNow", JBool(s.toBoolean))
    case JField("isPremoderated", JString(s)) => JField("isPremoderated", JBool(s.toBoolean))
    case JField("wordcount", JString(s)) => JField("wordcount", JInt(s.toInt))
    case JField("newspaperPageNumber", JString(s)) => JField("newspaperPageNumber", JInt(s.toInt))
    case JField("starRating", JString(s)) => JField("starRating", JInt(s.toInt))
    case JField("isInappropriateForSponsorship", JString(s)) => JField("isInappropriateForSponsorship", JBool(s.toBoolean))
    case JField("isInappropriateForAdverts", JString(s)) => JField("isInappropriateForAdverts", JBool(s.toBoolean))
    case JField("internalPageCode", JString(s)) => JField("internalPageCode", JInt(s.toInt))
    case JField("internalStoryPackageCode", JString(s)) => JField("internalStoryPackageCode", JInt(s.toInt))
    case JField("width", JString(s)) => JField("width", JInt(s.toInt))
    case JField("height", JString(s)) => JField("height", JInt(s.toInt))
    case JField("duration", JString(s)) => JField("duration", JInt(s.toInt))
    case JField("durationMinutes", JString(s)) => JField("durationMinutes", JInt(s.toInt))
    case JField("durationSeconds", JString(s)) => JField("durationSeconds", JInt(s.toInt))
    case JField("isMaster", JString(s)) => JField("isMaster", JBool(s.toBoolean))
    case JField("sizeInBytes", JString(s)) => JField("sizeInBytes", JLong(s.toLong))
    case JField("blockAds", JString(s)) => JField("blockAds", JBool(s.toBoolean))
    case JField("allowUgc", JString(s)) => JField("allowUgc", JBool(s.toBoolean))
    case JField("displayCredit", JString(s)) => JField("displayCredit", JBool(s.toBoolean))
    case JField("legallySensitive", JString(s)) => JField("legallySensitive", JBool(s.toBoolean))
    case JField("sensitive", JString(s)) => JField("sensitive", JBool(s.toBoolean))
    case JField("explicit", JString(s)) => JField("explicit", JBool(s.toBoolean))
    case JField("clean", JString(s)) => JField("clean", JBool(s.toBoolean))
    case JField("safeEmbedCode", JString(s)) => JField("safeEmbedCode", JBool(s.toBoolean))
    case JField("internalRevision", JString(s)) => JField("internalRevision", JInt(s.toInt))
    case JField("embeddable", JString(s)) => JField("embeddable", JBool(s.toBoolean))
  }

  def thriftEnum2JString[A <: ThriftEnum](implicit classTag: ClassTag[A]): PartialFunction[Any, JString] = {
    case a if classTag.runtimeClass.isInstance(a) => JString(pascalCaseToHyphenated(a.asInstanceOf[A].name))
  }

  private def stringifyRecursively(json: JValue): JValue = json transformField {
    case (k, JInt(value)) => k -> JString(value.toString)
    case (k, JBool(value)) => k -> JString(value.toString)
  }

  /**
    * Stringify some parts of the JSON representation of a Content
    * for compatibility with existing CAPI JSON response format.
    */
  def stringifyContent(content: JValue): JValue = {
    val stringifyRights = (content: JValue) => content.replace(List("rights"), stringifyRecursively(content \ "rights"))
    val stringifyFields = (content: JValue) => content.replace(List("fields"), stringifyRecursively(content \ "fields"))
    val stringifyElements = (content: JValue) => content.replace(List("elements"), stringifyRecursively(content \ "elements"))

    (stringifyRights andThen stringifyFields andThen stringifyElements)(content)
  }

  /**
    * Normalise a string for use in enum lookup by removing hyphens.
    * Note that Thrift enum lookup using the `valueOf` method is not case-sensitive.
    */
  private def removeHyphens(s: String): String = s.replaceAllLiterally("-", "")

  private val LowerCaseFollowedByUpperCase = """([a-z])([A-Z])""".r

  /**
    * Convert a PascalCase string to a lowercase hyphenated string
    */
  private def pascalCaseToHyphenated(s: String): String =
    LowerCaseFollowedByUpperCase.replaceAllIn(s, m => m.group(1) + "-" + m.group(2)).toLowerCase


  object AtomHelper {

    /**
      * This logic is necessary for deserializing atoms.
      * This is because the AtomData field is a union type, and we need to specify which type it is (e.g. AtomData.Quiz)
      */
    private def stringOpt(jValue: JValue): Option[String] =
      condOpt(jValue) {
        case JString(s) => s
      }

    private def getAtom(atom: JValue, atomData: AtomData): Option[Atom] = {
      for {
        id <- stringOpt(atom \ "id")
        atomType <- (atom \ "atomType").extractOpt[AtomType]
        labels <- (atom \ "labels").extractOpt[Seq[String]]
        defaultHtml <- stringOpt(atom \ "defaultHtml")
        change <- (atom \ "contentChangeDetails").extractOpt[ContentChangeDetails]
      } yield {
        Atom(id, atomType, labels, defaultHtml, atomData, change)
      }
    }

    private def getAtomData(atom: JObject, atomType: AtomType): Option[AtomData] = {
      atomType match {
        case AtomType.Quiz => Some(AtomData.Quiz((atom \ "data" \ "quiz").extract[QuizAtom]))
        case AtomType.Media => Some(AtomData.Media((atom \ "data" \ "media").extract[MediaAtom]))
        case AtomType.Explainer => Some(AtomData.Explainer((atom \ "data" \ "explainer").extract[ExplainerAtom]))
        case _ => None
      }
    }

    private def getAtomTypeFieldName(atomType: AtomType): Option[String] = {
      atomType match {
        case AtomType.Quiz => Some("quizzes")
        case AtomType.Media => Some("media")
        case AtomType.Explainer => Some("explainers")
        case _ => None
      }
    }

    //Get an Atom object from the given Atom json
    def getAtom(rawAtom: JObject): Option[Atom] = {
      for {
        atomType <- (rawAtom \ "atomType").extractOpt[AtomType]
        atomData <- getAtomData(rawAtom, atomType)
        atom <- getAtom(rawAtom, atomData)
      } yield atom
    }

    //Get a list of Atom objects from the given Atoms json for the given atom type
    def getAtoms(rawAtoms: JObject, atomType: AtomType): Option[Seq[Atom]] = {
      getAtomTypeFieldName(atomType) flatMap { fieldName =>
        (rawAtoms \ fieldName).extractOpt[Seq[JObject]] map { atoms =>
          atoms flatMap (atom => getAtomData(atom, atomType) flatMap (atomData => getAtom(atom, atomData)))
        }
      }
    }
  }

  import AtomHelper._

  object DummyAtomObject
  object AtomsSerializer extends CustomSerializer[Atoms](format => (
    {
      case rawAtoms: JObject => Atoms(quizzes = getAtoms(rawAtoms, AtomType.Quiz), media = getAtoms(rawAtoms, AtomType.Media),
        explainers = getAtoms(rawAtoms, AtomType.Explainer))
    },
    {
      PartialFunction.empty[Any, JValue]  //No custom serialization logic required for Atoms
    }
    ))

  object AtomSerializer extends CustomSerializer[Atom](format => (
    {
      case rawAtom: JObject => getAtom(rawAtom).get //...
    },
    {
      PartialFunction.empty[Any, JValue]  //No custom serialization logic required for Atom
    }
    ))

  object AtomTypeSerializer extends CustomSerializer[AtomType](format => (
    {
      case JString(s) => AtomType.valueOf(s).getOrElse(AtomType.EnumUnknownAtomType(-1))
    },
    thriftEnum2JString[AtomType]
    ))

  object MediaAtomAssetTypeSerializer extends CustomSerializer[MediaAtomAssetType](format => (
    {
      case JString(s) => MediaAtomAssetType.valueOf(s).getOrElse(MediaAtomAssetType.EnumUnknownAssetType(-1))
    },
    thriftEnum2JString[MediaAtomAssetType]
    ))

  object CategorySerializer extends CustomSerializer[Category](format => (
    {
      case JString(s) => Category.valueOf(s).getOrElse(Category.EnumUnknownCategory(-1))
    },
    thriftEnum2JString[Category]
    ))

  object DisplayTypeSerializer extends CustomSerializer[DisplayType](format => (
    {
      case JString(s) => DisplayType.valueOf(s).getOrElse(DisplayType.EnumUnknownDisplayType(-1))
    },
    thriftEnum2JString[DisplayType]
    ))

  object PlatformTypeSerializer extends CustomSerializer[Platform](format => (
    {
      case JString(s) => Platform.valueOf(s).getOrElse(Platform.EnumUnknownPlatform(-1))
    },
    thriftEnum2JString[Platform]
    ))

  object ContentTypeSerializer extends CustomSerializer[ContentType](format => (
    {
      case JString(s) => ContentType.valueOf(removeHyphens(s)).getOrElse(ContentType.EnumUnknownContentType(-1))
    },
    thriftEnum2JString[ContentType]
    ))

  object MembershipTierSerializer extends CustomSerializer[MembershipTier](format => (
    {
      case JString(s) => MembershipTier.valueOf(removeHyphens(s)).getOrElse(MembershipTier.EnumUnknownMembershipTier(-1))
    },
    thriftEnum2JString[MembershipTier]
    ))

  object OfficeSerializer extends CustomSerializer[Office](format => (
    {
      case JString(s) => Office.valueOf(removeHyphens(s)).getOrElse(Office.EnumUnknownOffice(-1))
    },
    thriftEnum2JString[Office].andThen(jstring => JString(jstring.s.toUpperCase))
    ))

  object AssetTypeSerializer extends CustomSerializer[AssetType](format => (
    {
      case JString(s) => AssetType.valueOf(removeHyphens(s)).getOrElse(AssetType.EnumUnknownAssetType(-1))
    },
    thriftEnum2JString[AssetType]
    ))

  object ElementTypeSerializer extends CustomSerializer[ElementType](format => (
    {
      case JString(s) => ElementType.valueOf(removeHyphens(s)).getOrElse(ElementType.EnumUnknownElementType(-1))
    },
    thriftEnum2JString[ElementType]
    ))

  object SponsorshipTypeSerializer extends CustomSerializer[SponsorshipType](format => (
    {
      case JString(s) => SponsorshipType.valueOf(removeHyphens(s)).getOrElse(SponsorshipType.EnumUnknownSponsorshipType(-1))
    },
    thriftEnum2JString[SponsorshipType]
    ))

  object TagTypeSerializer extends CustomSerializer[TagType](format => (
    {
      case JString(s) => TagType.valueOf(removeHyphens(s)).getOrElse(TagType.EnumUnknownTagType(-1))
    },
    thriftEnum2JString[TagType]
    ))

  object CrosswordTypeSerializer extends CustomSerializer[CrosswordType](format => (
    {
      case JString(s) => CrosswordType.valueOf(removeHyphens(s)).getOrElse(CrosswordType.EnumUnknownCrosswordType(-1))
    },
    thriftEnum2JString[CrosswordType]
    ))

  object StoryPackageArticleTypeSerializer extends CustomSerializer[ArticleType](format => (
    {
      case JString(s) => ArticleType.valueOf(removeHyphens(s)).getOrElse(ArticleType.EnumUnknownArticleType(-1))
    },
    thriftEnum2JString[ArticleType]
    ))

  object StoryPackageGroupSerializer extends CustomSerializer[Group](format => (
    {
      case JString(s) => Group.valueOf(removeHyphens(s)).getOrElse(Group.EnumUnknownGroup(-1))
    },
    thriftEnum2JString[Group]
    ))

  object DateTimeSerializer extends CustomSerializer[CapiDateTime](format => (
    {
      case JString(s) =>
        val dateTime = ISODateTimeFormat.dateOptionalTimeParser().withOffsetParsed().parseDateTime(s)
        CapiDateTime.apply(dateTime.getMillis, dateTime.toString(ISODateTimeFormat.dateTime()))
    },
    {
      case d: CapiDateTime =>
        val dateTime = ISODateTimeFormat.dateTime().withOffsetParsed().parseDateTime(d.iso8601)

        // We don't include millis in JSON, because nearest-second accuracy is enough for anyone!
        JString(dateTime.toString(ISODateTimeFormat.dateTimeNoMillis()))
    }
    ))

  object RemovePassthroughFieldsFromThriftStruct extends FieldSerializer[ThriftStruct](serializer = FieldSerializer.ignore("_passthroughFields"))

}
