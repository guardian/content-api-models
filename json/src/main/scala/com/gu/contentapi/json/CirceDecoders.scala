package com.gu.contentapi.json

import io.circe._
import io.circe.generic.semiauto._
import com.gu.contentapi.client.model.v1._
import com.gu.contentatom.thrift.atom._
import com.gu.contentatom.{thrift => contentatom}
import com.gu.contententity.thrift.entity._
import com.gu.contententity.{thrift => contententity}
import com.gu.fezziwig.CirceScroogeMacros.{decodeThriftEnum}
import com.gu.fezziwig.CirceScroogeWhiteboxMacros._
import com.gu.storypackage.model.{v1 => storypackage}
import cats.syntax.either._
import java.time.OffsetDateTime
import java.time.chrono.IsoChronology
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder, ResolverStyle}
import java.time.temporal.ChronoField

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

  //We need this custom formatter because some fields in capi only have the date
  val formatter = new DateTimeFormatterBuilder()
    .parseCaseInsensitive()
    .append(DateTimeFormatter.ISO_LOCAL_DATE)
    .optionalStart
    .appendLiteral('T')
    .append(DateTimeFormatter.ISO_LOCAL_TIME)
    .optionalStart
    .appendFraction(ChronoField.NANO_OF_SECOND, 3, 3, false)
    .optionalEnd
    .optionalStart
    .appendOffsetId
    .optionalEnd
    .optionalEnd
    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
    .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
    .parseDefaulting(ChronoField.OFFSET_SECONDS, 0)
    .toFormatter

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
          val dateTime = OffsetDateTime.parse(dateTimeString, formatter)
          Either.right(CapiDateTime.apply(dateTime.toInstant.toEpochMilli(), DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dateTime)))
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

  implicit val contentFieldsDecoder: Decoder[ContentFields] = deriveDecoder
  implicit val editionDecoder: Decoder[Edition] = deriveDecoder
  implicit val sponsorshipDecoder: Decoder[Sponsorship] = deriveDecoder
  implicit val sponsorshipTargetingDecoder: Decoder[SponsorshipTargeting] = deriveDecoder
  implicit val sponsorshipLogoDimensionsDecoder: Decoder[SponsorshipLogoDimensions] = deriveDecoder
  implicit val tagDecoder: Decoder[Tag] = deriveDecoder
  implicit val tagTypeDecoder: Decoder[TagType] = deriveDecoder
  implicit val podcastDecoder: Decoder[Podcast] = deriveDecoder
  implicit val podcastCategoryDecoder: Decoder[PodcastCategory] = deriveDecoder
  implicit val assetDecoder: Decoder[Asset] = deriveDecoder
  implicit val assetTypeDecoder: Decoder[AssetType] = deriveDecoder
  implicit val assetFieldsDecoder: Decoder[AssetFields] = deriveDecoder
  implicit val cartoonVariantDecoder: Decoder[CartoonVariant] = deriveDecoder
  implicit val cartoonImageDecoder: Decoder[CartoonImage] = deriveDecoder
  implicit val elementDecoder: Decoder[Element] = deriveDecoder
  implicit val referenceDecoder: Decoder[Reference] = deriveDecoder
  implicit val blockElementDecoder: Decoder[BlockElement] = deriveDecoder
  implicit val textElementFieldsDecoder: Decoder[TextElementFields] = deriveDecoder
  implicit val videoElementFieldsDecoder: Decoder[VideoElementFields] = deriveDecoder
  implicit val tweetElementFieldsDecoder: Decoder[TweetElementFields] = deriveDecoder
  implicit val imageElementFieldsDecoder: Decoder[ImageElementFields] = deriveDecoder
  implicit val audioElementFieldsDecoder: Decoder[AudioElementFields] = deriveDecoder
  implicit val pullquoteElementFieldsDecoder: Decoder[PullquoteElementFields] = deriveDecoder
  implicit val interactiveElementFieldsDecoder: Decoder[InteractiveElementFields] = deriveDecoder
  implicit val standardElementFieldsDecoder: Decoder[StandardElementFields] = deriveDecoder
  implicit val witnessElementFieldsDecoder: Decoder[WitnessElementFields] = deriveDecoder
  implicit val richLinkElementFieldsDecoder: Decoder[RichLinkElementFields] = deriveDecoder
  implicit val membershipElementFieldsDecoder: Decoder[MembershipElementFields] = deriveDecoder
  implicit val embedElementFieldsDecoder: Decoder[EmbedElementFields] = deriveDecoder
  implicit val instagramElementFieldsDecoder: Decoder[InstagramElementFields] = deriveDecoder
  implicit val commentElementFieldsDecoder: Decoder[CommentElementFields] = deriveDecoder
  implicit val vineElementFieldsDecoder: Decoder[VineElementFields] = deriveDecoder
  implicit val contentAtomElementFieldsDecoder: Decoder[ContentAtomElementFields] = deriveDecoder
  implicit val embedTrackingDecoder: Decoder[EmbedTracking] = deriveDecoder
  implicit val codeElementFieldsDecoder: Decoder[CodeElementFields] = deriveDecoder
  implicit val calloutElementFieldsDecoder: Decoder[CalloutElementFields] = deriveDecoder
  implicit val cartoonElementFieldsDecoder: Decoder[CartoonElementFields] = deriveDecoder
  implicit val recipeElementFieldsDecoder: Decoder[RecipeElementFields] = deriveDecoder
  implicit val listElementFieldsDecoder: Decoder[ListElementFields] = deriveDecoder
  implicit val listItemDecoder: Decoder[ListItem] = deriveDecoder
  implicit val blockDecoder: Decoder[Block] = deriveDecoder
  implicit val blockAttributesDecoder: Decoder[BlockAttributes] = deriveDecoder
  implicit val membershipPlaceholderDecoder: Decoder[MembershipPlaceholder] = deriveDecoder
  implicit val userDecoder: Decoder[User] = deriveDecoder
  implicit val blocksDecoder: Decoder[Blocks] = deriveDecoder
  implicit val rightsDecoder: Decoder[Rights] = deriveDecoder
  implicit val crosswordEntryDecoder: Decoder[CrosswordEntry] = deriveDecoder
  implicit val crosswordPositionDecoder: Decoder[CrosswordPosition] = deriveDecoder
  implicit val crosswordDecoder: Decoder[Crossword] = deriveDecoder
  implicit val crosswordDimensionsDecoder: Decoder[CrosswordDimensions] = deriveDecoder
  implicit val crosswordCreatorDecoder: Decoder[CrosswordCreator] = deriveDecoder
  implicit val contentStatsDecoder: Decoder[ContentStats] = deriveDecoder
  implicit val sectionDecoder: Decoder[Section] = deriveDecoder
  implicit val debugDecoder: Decoder[Debug] = deriveDecoder
  implicit val atomTypeDecoder: Decoder[contentatom.AtomType] = deriveDecoder
  implicit val atomDataDecoder: Decoder[contentatom.AtomData] = deriveDecoder
  implicit val quizAtomDecoder: Decoder[quiz.QuizAtom] = deriveDecoder
  implicit val quizContentDecoder: Decoder[quiz.QuizContent] = deriveDecoder
  implicit val questionDecoder: Decoder[quiz.Question] = deriveDecoder
  implicit val answerDecoder: Decoder[quiz.Answer] = deriveDecoder
  implicit val quizAssetDecoder: Decoder[quiz.Asset] = deriveDecoder
  implicit val resultGroupsDecoder: Decoder[quiz.ResultGroups] = deriveDecoder
  implicit val resultGroupDecoder: Decoder[quiz.ResultGroup] = deriveDecoder
  implicit val resultBucketsDecoder: Decoder[quiz.ResultBuckets] = deriveDecoder
  implicit val resultBucketDecoder: Decoder[quiz.ResultBucket] = deriveDecoder
  implicit val mediaAtomDecoder: Decoder[media.MediaAtom] = deriveDecoder
  implicit val imageDecoder: Decoder[contentatom.Image] = deriveDecoder
  implicit val imageAssetDecoder: Decoder[contentatom.ImageAsset] = deriveDecoder
  implicit val imageAssetDimensionsDecoder: Decoder[contentatom.ImageAssetDimensions] = deriveDecoder
  implicit val mediaAssetDecoder: Decoder[media.Asset] = deriveDecoder
  implicit val mediaMetadataDecoder: Decoder[media.Metadata] = deriveDecoder
  implicit val mediaPlutoDataDecoder: Decoder[media.PlutoData] = deriveDecoder
  implicit val mediaYoutubeDataDecoder: Decoder[media.YoutubeData] = deriveDecoder
  implicit val explainerAtomDecoder: Decoder[explainer.ExplainerAtom] = deriveDecoder
  implicit val ctaAtomDecoder: Decoder[cta.CTAAtom] = deriveDecoder
  implicit val interactiveAtomDecoder: Decoder[interactive.InteractiveAtom] = deriveDecoder
  implicit val reviewAtomDecoder: Decoder[review.ReviewAtom] = deriveDecoder
  implicit val ratingDecoder: Decoder[review.Rating] = deriveDecoder
  implicit val restaurantDecoder: Decoder[restaurant.Restaurant] = deriveDecoder
  implicit val addressDecoder: Decoder[contententity.Address] = deriveDecoder
  implicit val geolocationDecoder: Decoder[contententity.Geolocation] = deriveDecoder
  implicit val gameDecoder: Decoder[game.Game] = deriveDecoder
  implicit val priceDecoder: Decoder[contententity.Price] = deriveDecoder
  implicit val filmDecoder: Decoder[film.Film] = deriveDecoder
  implicit val personDecoder: Decoder[person.Person] = deriveDecoder
  implicit val qAndAAtomDecoder: Decoder[qanda.QAndAAtom] = deriveDecoder
  implicit val qAndAItemDecoder: Decoder[qanda.QAndAItem] = deriveDecoder
  implicit val guideAtomDecoder: Decoder[guide.GuideAtom] = deriveDecoder
  implicit val guideItemDecoder: Decoder[guide.GuideItem] = deriveDecoder
  implicit val profileAtomDecoder: Decoder[profile.ProfileAtom] = deriveDecoder
  implicit val profileItemDecoder: Decoder[profile.ProfileItem] = deriveDecoder
  implicit val timelineAtomDecoder: Decoder[timeline.TimelineAtom] = deriveDecoder
  implicit val timelineItemDecoder: Decoder[timeline.TimelineItem] = deriveDecoder
  implicit val commonsDivisionDecoder: Decoder[commonsdivision.CommonsDivision] = deriveDecoder
  implicit val votesDecoder: Decoder[commonsdivision.Votes] = deriveDecoder
  implicit val mpDecoder: Decoder[commonsdivision.Mp] = deriveDecoder
  implicit val chartAtomDecoder: Decoder[chart.ChartAtom] = deriveDecoder
  implicit val furnitureDecoder: Decoder[chart.Furniture] = deriveDecoder
  implicit val tabularDataDecoder: Decoder[chart.TabularData] = deriveDecoder
  implicit val seriesColourDecoder: Decoder[chart.SeriesColour] = deriveDecoder
  implicit val axisDecoder: Decoder[chart.Axis] = deriveDecoder
  implicit val rangeDecoder: Decoder[chart.Range] = deriveDecoder
  implicit val displaySettingsDecoder: Decoder[chart.DisplaySettings] = deriveDecoder
  implicit val audioAtomDecoder: Decoder[audio.AudioAtom] = deriveDecoder
  implicit val offPlatformDecoder: Decoder[audio.OffPlatform] = deriveDecoder
  implicit val emailSignUpAtomDecoder: Decoder[emailsignup.EmailSignUpAtom] = deriveDecoder
  implicit val atomDecoder: Decoder[contentatom.Atom] = deriveDecoder
  implicit val contentChangeDetailsDecoder: Decoder[contentatom.ContentChangeDetails] = deriveDecoder
  implicit val changeRecordDecoder: Decoder[contentatom.ChangeRecord] = deriveDecoder
  implicit val contentatomUserDecoder: Decoder[contentatom.User] = deriveDecoder
  implicit val flagsDecoder: Decoder[contentatom.Flags] = deriveDecoder
  implicit val atomsDecoder: Decoder[Atoms] = deriveDecoder
  implicit val pillarDecoder: Decoder[Pillar] = deriveDecoder
  implicit val contentDecoder: Decoder[Content] = deriveDecoder
  implicit val aliasPathDecoder: Decoder[AliasPath] = deriveDecoder
  implicit val contentChannelDecoder: Decoder[ContentChannel] = deriveDecoder
  implicit val channelFieldsDecoder: Decoder[ChannelFields] = deriveDecoder
  implicit val mostViewedVideoDecoder: Decoder[MostViewedVideo] = deriveDecoder
  implicit val networkFrontDecoder: Decoder[NetworkFront] = deriveDecoder
  implicit val packageArticleDecoder: Decoder[PackageArticle] = deriveDecoder
  implicit val articleDecoder: Decoder[storypackage.Article] = deriveDecoder
  implicit val packageDecoder: Decoder[Package] = deriveDecoder
  implicit val itemResponseDecoder: Decoder[ItemResponse] = deriveDecoder
  implicit val searchResponseDecoder: Decoder[SearchResponse] = deriveDecoder
  implicit val editionsResponseDecoder: Decoder[EditionsResponse] = deriveDecoder
  implicit val tagsResponseDecoder: Decoder[TagsResponse] = deriveDecoder
  implicit val sectionsResponseDecoder: Decoder[SectionsResponse] = deriveDecoder
  implicit val atomsResponseDecoder: Decoder[AtomsResponse] = deriveDecoder
  implicit val packagesResponseDecoder: Decoder[PackagesResponse] = deriveDecoder
  implicit val errorResponseDecoder: Decoder[ErrorResponse] = deriveDecoder
  implicit val videoStatsResponseDecoder: Decoder[VideoStatsResponse] = deriveDecoder
  implicit val atomsUsageResponseDecoder: Decoder[AtomUsageResponse] = deriveDecoder
  implicit val entityDecoder: Decoder[contententity.Entity] = deriveDecoder
  implicit val placeDecoder: Decoder[place.Place] = deriveDecoder
  implicit val organisationDecoder: Decoder[organisation.Organisation] = deriveDecoder
  implicit val entitiesResponseDecoder: Decoder[EntitiesResponse] = deriveDecoder
  implicit val pillarsResponseDecoder: Decoder[PillarsResponse] = deriveDecoder
  implicit val embedReachDecoder: Decoder[EmbedReach] = deriveDecoder
}
