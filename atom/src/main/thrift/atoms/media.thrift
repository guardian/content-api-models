namespace * contentatom.media
namespace java com.gu.contentatom.thrift.atom.media
#@namespace scala com.gu.contentatom.thrift.atom.media

include "../shared.thrift"

typedef i64 Version

enum Platform {
  YOUTUBE = 0,
  FACEBOOK = 1,
  DAILYMOTION = 2,
  MAINSTREAM = 3,
  URL = 4
}

enum AssetType {
  AUDIO = 0,
  VIDEO = 1
}

enum Category {
  DOCUMENTARY = 0,
  EXPLAINER = 1,
  FEATURE = 2,
  NEWS = 3,
  HOSTED = 4, // commercial content supplied by advertiser
  PAID = 5 // commercial content paid for by third-party
}

/** how a YouTube video can be watched **/
enum PrivacyStatus {
   PRIVATE = 0, // requires login, not returned by search
   UNLISTED = 1, // requires knowledge of URL, not returned by search
   PUBLIC = 2 // can be viewed and found by search
}

struct Asset {
  1: required AssetType assetType
  2: required Version version
  3: required string id
  4: required Platform platform
  5: optional string mimeType
}

struct PlutoData {
  1: optional string commissionId
  2: optional string projectId
  3: optional string masterId
}

struct Metadata {
  /**
    * tags to be applied to the YouTube video
    * https://developers.google.com/youtube/v3/docs/videos#snippet.tags[]
    * **/
  1: optional list<string> tags

  /**
    * YouTube video category
    * https://developers.google.com/youtube/v3/docs/videos#snippet.categoryId
    * **/
  2: optional string categoryId

  3: optional string license

  /** are comments enabled on the YouTube video **/
  4: optional bool commentsEnabled

  /** the channel the YouTube video is in **/
  5: optional string channelId

  /** how a YouTube video can be watched **/
  6: optional PrivacyStatus privacyStatus

  /** when the PrivacyStatus will change to Private **/
  7: optional shared.DateTime expiryDate

  /** where the master is in Pluto **/
  8: optional PlutoData pluto
}

struct MediaAtom {
  2: required list<Asset> assets
  3: optional Version activeVersion

  /** video title aka headline **/
  4: required string title

  5: required Category category

  /** DEPRECATED in favour of `Metadata.pluto` **/
  6: optional string plutoProjectId

  7: optional i64 duration // seconds

  /** video source **/
  8: optional string source

  /** DEPRECATED **/
  9: optional string posterUrl

  /** video description aka standfirst **/
  10: optional string description

  11: optional Metadata metadata

  /** poster image of the video on YouTube and in article **/
  12: optional shared.Image posterImage

  /** trail text of the (optional) Composer page **/
  14: optional string trailText

  /** byline of the (optional) Composer page **/
  15: optional list<string> byline

  /** commissioning desk of the (optional) Composer page **/
  16: optional list<string> commissioningDesks

  /** tags of the (optional) Composer page **/
  17: optional list<string> keywords

  /** trail image of the (optional) Composer page **/
  18: optional shared.Image trailImage

  /** optimised for web of the (optional) Composer page **/
  19: optional bool optimisedForWeb

  /** comments enabled of the (optional) Composer page **/
  20: optional bool commentsEnabled

  /** suppress related content of the (optional) Composer page **/
  21: optional bool suppressRelatedContent

  /** alt text for Composer page trail image**/
  22: optional string altText
}
