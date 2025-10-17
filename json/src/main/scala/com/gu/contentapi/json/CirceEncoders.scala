package com.gu.contentapi.json

import com.gu.contentapi.client.model.v1._
import com.gu.contentapi.client.model.schemaorg.{SchemaOrg, SchemaRecipe, AuthorInfo, RecipeStep}
import com.gu.contentatom.thrift.atom._
import com.gu.contentatom.{thrift => contentatom}
import com.gu.contententity.thrift.entity._
import com.gu.contententity.{thrift => contententity}
import com.twitter.scrooge.ThriftEnum
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._
import com.gu.fezziwig.CirceScroogeWhiteboxMacros._
import com.gu.storypackage.model.{v1 => storypackage}
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

  implicit val dateTimeEncoder: Encoder[CapiDateTime] = genDateTimeEncoder()
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

  implicit val contentFieldsEncoder: Encoder[ContentFields] = deriveEncoder
  implicit val editionEncoder: Encoder[Edition] = deriveEncoder
  implicit val sponsorshipEncoder: Encoder[Sponsorship] = deriveEncoder
  implicit val sponsorshipTargetingEncoder: Encoder[SponsorshipTargeting] = deriveEncoder
  implicit val sponsorshipLogoDimensionsEncoder: Encoder[SponsorshipLogoDimensions] = deriveEncoder
  implicit val tagEncoder: Encoder[Tag] = deriveEncoder
  implicit val podcastEncoder: Encoder[Podcast] = deriveEncoder
  implicit val podcastCategoryEncoder: Encoder[PodcastCategory] = deriveEncoder
  implicit val assetEncoder: Encoder[Asset] = deriveEncoder
  implicit val assetFieldsEncoder: Encoder[AssetFields] = deriveEncoder
  implicit val cartoonVariantEncoder: Encoder[CartoonVariant] = deriveEncoder
  implicit val cartoonImageEncoder: Encoder[CartoonImage] = deriveEncoder
  implicit val elementEncoder: Encoder[Element] = deriveEncoder
  implicit val referenceEncoder: Encoder[Reference] = deriveEncoder
  implicit val blockElementEncoder: Encoder[BlockElement] = deriveEncoder
  implicit val textElementFieldsEncoder: Encoder[TextElementFields] = deriveEncoder
  implicit val videoElementFieldsEncoder: Encoder[VideoElementFields] = deriveEncoder
  implicit val tweetElementFieldsEncoder: Encoder[TweetElementFields] = deriveEncoder
  implicit val imageElementFieldsEncoder: Encoder[ImageElementFields] = deriveEncoder
  implicit val audioElementFieldsEncoder: Encoder[AudioElementFields] = deriveEncoder
  implicit val pullquoteElementFieldsEncoder: Encoder[PullquoteElementFields] = deriveEncoder
  implicit val interactiveElementFieldsEncoder: Encoder[InteractiveElementFields] = deriveEncoder
  implicit val standardElementFieldsEncoder: Encoder[StandardElementFields] = deriveEncoder
  implicit val witnessElementFieldsEncoder: Encoder[WitnessElementFields] = deriveEncoder
  implicit val richLinkElementFieldsEncoder: Encoder[RichLinkElementFields] = deriveEncoder
  implicit val membershipElementFieldsEncoder: Encoder[MembershipElementFields] = deriveEncoder
  implicit val embedElementFieldsEncoder: Encoder[EmbedElementFields] = deriveEncoder
  implicit val instagramElementFieldsEncoder: Encoder[InstagramElementFields] = deriveEncoder
  implicit val commentElementFieldsEncoder: Encoder[CommentElementFields] = deriveEncoder
  implicit val vineElementFieldsEncoder: Encoder[VineElementFields] = deriveEncoder
  implicit val contentAtomElementFieldsEncoder: Encoder[ContentAtomElementFields] = deriveEncoder
  implicit val embedTrackingEncoder: Encoder[EmbedTracking] = deriveEncoder
  implicit val codeElementFieldsEncoder: Encoder[CodeElementFields] = deriveEncoder
  implicit val calloutElementFieldsEncoder: Encoder[CalloutElementFields] = deriveEncoder
  implicit val cartoonElementFieldsEncoder: Encoder[CartoonElementFields] = deriveEncoder
  implicit val recipeElementFieldsEncoder: Encoder[RecipeElementFields] = deriveEncoder
  implicit val listElementFieldsEncoder: Encoder[ListElementFields] = deriveEncoder
  implicit val listItemEncoder: Encoder[ListItem] = deriveEncoder
  implicit val timelineElementFieldsEncoder: Encoder[TimelineElementFields] = deriveEncoder
  implicit val timelineSectionEncoder: Encoder[TimelineSection] = deriveEncoder
  implicit val timelineEventEncoder: Encoder[TimelineEvent] = deriveEncoder
  implicit val blockEncoder: Encoder[Block] = deriveEncoder
  implicit val blockAttributesEncoder: Encoder[BlockAttributes] = deriveEncoder
  implicit val membershipPlaceholderEncoder: Encoder[MembershipPlaceholder] = deriveEncoder
  implicit val userEncoder: Encoder[User] = deriveEncoder
  implicit val blocksEncoder: Encoder[Blocks] = deriveEncoder // changed
  implicit val rightsEncoder: Encoder[Rights] = deriveEncoder
  implicit val crosswordEntryEncoder: Encoder[CrosswordEntry] = deriveEncoder // changed
  implicit val crosswordPositionEncoder: Encoder[CrosswordPosition] = deriveEncoder
  implicit val crosswordEncoder: Encoder[Crossword] = deriveEncoder
  implicit val crosswordDimensionsEncoder: Encoder[CrosswordDimensions] = deriveEncoder
  implicit val crosswordCreatorEncoder: Encoder[CrosswordCreator] = deriveEncoder
  implicit val contentStatsEncoder: Encoder[ContentStats] = deriveEncoder
  implicit val sectionEncoder: Encoder[Section] = deriveEncoder
  implicit val atomDataEncoder: Encoder[contentatom.AtomData] = deriveEncoder
  implicit val quizAtomEncoder: Encoder[quiz.QuizAtom] = deriveEncoder
  implicit val quizContentEncoder: Encoder[quiz.QuizContent] = deriveEncoder
  implicit val questionEncoder: Encoder[quiz.Question] = deriveEncoder
  implicit val answerEncoder: Encoder[quiz.Answer] = deriveEncoder
  implicit val quizAssetEncoder: Encoder[quiz.Asset] = deriveEncoder
  implicit val resultGroupsEncoder: Encoder[quiz.ResultGroups] = deriveEncoder
  implicit val resultGroupEncoder: Encoder[quiz.ResultGroup] = deriveEncoder
  implicit val resultBucketsEncoder: Encoder[quiz.ResultBuckets] = deriveEncoder
  implicit val resultBucketEncoder: Encoder[quiz.ResultBucket] = deriveEncoder
  implicit val mediaAtomEncoder: Encoder[media.MediaAtom] = deriveEncoder
  implicit val imageEncoder: Encoder[contentatom.Image] = deriveEncoder
  implicit val imageAssetEncoder: Encoder[contentatom.ImageAsset] = deriveEncoder
  implicit val imageAssetDimensionsEncoder: Encoder[contentatom.ImageAssetDimensions] = deriveEncoder
  implicit val mediaAssetEncoder: Encoder[media.Asset] = deriveEncoder
  implicit val mediaMetadataEncoder: Encoder[media.Metadata] = deriveEncoder
  implicit val mediaPlutoDataEncoder: Encoder[media.PlutoData] = deriveEncoder
  implicit val mediaYoutubeDataEncoder: Encoder[media.YoutubeData] = deriveEncoder
  implicit val explainerAtomEncoder: Encoder[explainer.ExplainerAtom] = deriveEncoder
  implicit val ctaAtomEncoder: Encoder[cta.CTAAtom] = deriveEncoder
  implicit val interactiveAtomEncoder: Encoder[interactive.InteractiveAtom] = deriveEncoder
  implicit val reviewAtomEncoder: Encoder[review.ReviewAtom] = deriveEncoder
  implicit val ratingEncoder: Encoder[review.Rating] = deriveEncoder
  implicit val restaurantEncoder: Encoder[restaurant.Restaurant] = deriveEncoder
  implicit val addressEncoder: Encoder[contententity.Address] = deriveEncoder
  implicit val geolocationEncoder: Encoder[contententity.Geolocation] = deriveEncoder
  implicit val gameEncoder: Encoder[game.Game] = deriveEncoder
  implicit val priceEncoder: Encoder[contententity.Price] = deriveEncoder
  implicit val filmEncoder: Encoder[film.Film] = deriveEncoder
  implicit val personEncoder: Encoder[person.Person] = deriveEncoder
  implicit val qAndAAtomEncoder: Encoder[qanda.QAndAAtom] = deriveEncoder
  implicit val qAndAItemEncoder: Encoder[qanda.QAndAItem] = deriveEncoder
  implicit val guideAtomEncoder: Encoder[guide.GuideAtom] = deriveEncoder
  implicit val guideItemEncoder: Encoder[guide.GuideItem] = deriveEncoder
  implicit val profileAtomEncoder: Encoder[profile.ProfileAtom] = deriveEncoder
  implicit val profileItemEncoder: Encoder[profile.ProfileItem] = deriveEncoder
  implicit val timelineAtomEncoder: Encoder[timeline.TimelineAtom] = deriveEncoder
  implicit val timelineItemEncoder: Encoder[timeline.TimelineItem] = deriveEncoder
  implicit val commonsDivisionEncoder: Encoder[commonsdivision.CommonsDivision] = deriveEncoder
  implicit val votesEncoder: Encoder[commonsdivision.Votes] = deriveEncoder
  implicit val mpEncoder: Encoder[commonsdivision.Mp] = deriveEncoder
  implicit val chartAtomEncoder: Encoder[chart.ChartAtom] = deriveEncoder
  implicit val furnitureEncoder: Encoder[chart.Furniture] = deriveEncoder
  implicit val tabularDataEncoder: Encoder[chart.TabularData] = deriveEncoder
  implicit val seriesColourEncoder: Encoder[chart.SeriesColour] = deriveEncoder
  implicit val axisEncoder: Encoder[chart.Axis] = deriveEncoder
  implicit val rangeEncoder: Encoder[chart.Range] = deriveEncoder
  implicit val displaySettingsEncoder: Encoder[chart.DisplaySettings] = deriveEncoder
  implicit val audioAtomEncoder: Encoder[audio.AudioAtom] = deriveEncoder
  implicit val offPlatformEncoder: Encoder[audio.OffPlatform] = deriveEncoder
  implicit val emailSignUpAtomEncoder: Encoder[emailsignup.EmailSignUpAtom] = deriveEncoder
  implicit val atomEncoder: Encoder[contentatom.Atom] = deriveEncoder
  implicit val contentChangeDetailsEncoder: Encoder[contentatom.ContentChangeDetails] = deriveEncoder
  implicit val changeRecordEncoder: Encoder[contentatom.ChangeRecord] = deriveEncoder
  implicit val contentatomUserEncoder: Encoder[contentatom.User] = deriveEncoder
  implicit val flagsEncoder: Encoder[contentatom.Flags] = deriveEncoder
  implicit val atomsEncoder: Encoder[Atoms] = deriveEncoder
  implicit val pillarEncoder: Encoder[Pillar] = deriveEncoder
  implicit val contentEncoder: Encoder[Content] = deriveEncoder
  implicit val aliasPathEncoder: Encoder[AliasPath] = deriveEncoder
  implicit val schemaOrgEncoder: Encoder[SchemaOrg] = deriveEncoder
  implicit val schemaRecipeEncoder: Encoder[SchemaRecipe] = deriveEncoder
  implicit val recipeStepEncoder: Encoder[RecipeStep] = deriveEncoder
  implicit val authorInfoEncoder: Encoder[AuthorInfo] = deriveEncoder
  implicit val contentChannelEncoder: Encoder[ContentChannel] = deriveEncoder
  implicit val channelFieldsEncoder: Encoder[ChannelFields] = deriveEncoder
  implicit val mostViewedVideoEncoder: Encoder[MostViewedVideo] = deriveEncoder
  implicit val networkFrontEncoder: Encoder[NetworkFront] = deriveEncoder
  implicit val packageArticleEncoder: Encoder[PackageArticle] = deriveEncoder
  implicit val articleEncoder: Encoder[storypackage.Article] = deriveEncoder
  implicit val packageEncoder: Encoder[Package] = deriveEncoder
  implicit val itemResponseEncoder: Encoder[ItemResponse] = deriveEncoder
  implicit val searchResponseEncoder: Encoder[SearchResponse] = deriveEncoder
  implicit val editionsResponseEncoder: Encoder[EditionsResponse] = deriveEncoder
  implicit val tagsResponseEncoder: Encoder[TagsResponse] = deriveEncoder
  implicit val sectionsResponseEncoder: Encoder[SectionsResponse] = deriveEncoder
  implicit val atomsResponseEncoder: Encoder[AtomsResponse] = deriveEncoder
  implicit val packagesResponseEncoder: Encoder[PackagesResponse] = deriveEncoder
  implicit val errorResponseEncoder: Encoder[ErrorResponse] = deriveEncoder
  implicit val videoStatsResponseEncoder: Encoder[VideoStatsResponse] = deriveEncoder
  implicit val atomsUsageResponseEncoder: Encoder[AtomUsageResponse] = deriveEncoder
  implicit val entityEncoder: Encoder[contententity.Entity] = deriveEncoder
  implicit val placeEncoder: Encoder[place.Place] = deriveEncoder
  implicit val organisationEncoder: Encoder[organisation.Organisation] = deriveEncoder
  implicit val entitiesResponseEncoder: Encoder[EntitiesResponse] = deriveEncoder
  implicit val pillarsResponseEncoder: Encoder[PillarsResponse] = deriveEncoder
  implicit val embedReachEncoder: Encoder[EmbedReach] = deriveEncoder
  implicit val linkElementFieldsEncoder: Encoder[LinkElementFields] = deriveEncoder
  implicit val productCustomAttributeEncoder: Encoder[ProductCustomAttribute] = deriveEncoder
  implicit val productCtaEncoder: Encoder[ProductCTA] = deriveEncoder
  implicit val productImageEncoder: Encoder[ProductImage] = deriveEncoder
  implicit val productElementFieldsEncoder: Encoder[ProductElementFields] = deriveEncoder

  def genDateTimeEncoder(truncate: Boolean = true): Encoder[CapiDateTime] = Encoder.instance[CapiDateTime] { capiDateTime =>
    val dateTime: OffsetDateTime = OffsetDateTime.parse(capiDateTime.iso8601)
    // We don't include millis in JSON, for backwards-compatibility
    Json.fromString(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(if (truncate) dateTime.truncatedTo(ChronoUnit.SECONDS) else dateTime))
  }
}
