include "image.thrift"
include "sponsorship.thrift"


namespace scala com.gu.tagmanagement

/** the types of tags supported */
enum TagType {
    TOPIC = 0,
    CONTRIBUTOR = 1,
    SERIES = 2,
    TONE = 3,
    CONTENT_TYPE = 4,
    PUBLICATION = 5,
    NEWSPAPER_BOOK = 6,
    NEWSPAPER_BOOK_SECTION = 7,
    BLOG = 8,
    TRACKING = 9,
    PAID_CONTENT = 10,
    CAMPAIGN = 11
}

struct PodcastCategory {
    /** The iTunes main category  **/
    1: required string main
    
    /** The iTunes sub category  **/
    2: optional string sub
}

struct PodcastMetadata {
    /** The iTunes link URL **/
    1: required string linkUrl

    /** The iTunes copyright text **/
    2: optional string copyrightText

    /** The iTunes author text **/
    3: optional string authorText

    /** The iTunes url for the podcast **/
    4: optional string iTunesUrl

    /** Should the podcast appear in iTunes **/
    5: required bool iTunesBlock

    /** Should the podcast be marked as clean in iTunes **/
    6: required bool clean

    /** Should the podcast be marked as explicit in iTunes **/
    7: required bool explicit

    /** iTunes podcast image **/
    8: optional image.Image image
    
    /** iTunes category **/
    9: optional list<PodcastCategory> categories

    10: optional string podcastType

    /** The Google Podcasts url for the podcast **/
    11: optional string googlePodcastsUrl

    12: optional string spotifyUrl
}

struct ContributorInformation {
    /** Any RCS id associated with the tag */
    1: optional string rcsId;

    /** Any byline image associated with the tag   */
    2: optional image.Image bylineImage;

    /** Any Large byline image associated with the tag */
    3: optional image.Image largeBylineImage;

    /** Twitter Handle associated with the tag */
    4: optional string twitterHandle;

    /** Contact Email for contributors */
    5: optional string contactEmail;

    /** The contributor's first name used for indexing **/
    6: optional string  firstName;

    /** The contributor's last name used for indexing **/
    7: optional string lastName;

}

struct PublicationInformation {
  /** The main newpaper book section associated with the publication */
  1: optional i64 mainNewspaperBookSectionId;

  /** Any newspaper books associated with the publication */
  2: required set<i64> newspaperBooks;
}

struct TrackingInformation {
    /** The type of tracking tag that this is, e.g. Commissioning Desk */
    1: required string trackingType;
}

struct PaidContentInformation {
    /** The sub type that this paid content is */
    1: required string paidContentType;
    /** A custom colour used by the campaign in hex format e.g. #FF00FF */
    2: optional string campaignColour;
}

struct CampaignInformation {
    /** The type of campaign tag that this is, e.g. Callout */
    1: required string campaignType;
}

struct Reference {
    /** the type of the the reference, e.g. musicbrainz, imdb, pa football team etc. */
    1: required string type;

    /** the value to the reference */
    2: required string value;

    /** the capi type for this reference, missing value indicates not present in CAPI */
    3: optional string capiType;
}

struct Tag {

    /** the id of the tag */
    1: required i64 id;

    /** the path of the canonical page for this tag - also used as api identifier*/
    2: required string path;

    /** the page id for the path as asigned by the path manager */
    3: required i64 pageId;

    /** the type of this tag */
    4: required TagType type;

    /** the internal label for the tag */
    5: required string internalName;

    /** the publicly displayed name for the tag */
    6: required string externalName;

    /** the tag owned url slug, the path is derived from this, the section and the tag type */
    7: required string slug;

    /** the tag is not displayed on the site if hidden is true */
    8: required bool hidden;

    /** legally sensitive tags surpress content showing related content and showing up in related content results */
    9: required bool legallySensitive;

    /** The natural sort key used for ordering tag lists, puts surname first, removes 'the' etc. */
    10: required string comparableValue;

    /** the id of the section this tag belongs to, if missing the tag is in the 'Global' pseudosection */
    11: optional i64 section;

    /** the id of the publication this tag belongs to, generally used for newspaper_book type tags */
    12: optional i64 publication;

    /** a description of the tag, rendered on tag pages, this could be a topic precis or a contributor's profile */
    13: optional string description;

    /** a set of parent tag ids. NB a tag can have multiple parents, but more often the set will be empty */
    14: required set<i64> parents;

    /** the reference mappings for this tag */
    15: required list<Reference> references;

    /** Any Podcast Metadata associated with this tag */
    16: optional PodcastMetadata podcastMetadata;

    /** Any Contributor Information associated with this tag */
    17: optional ContributorInformation contributorInformation;

    /** Any Publication Information associated with this tag (Only for Publication Tags) */
    18: optional PublicationInformation publicationInformation;

    /** Is this tag a microsite tag? (In a microsite section when migrated) */
    19: required bool isMicrosite;

    /** CAPI specific field containing their section id */
    20: optional string capiSectionId;

    /** Any trackinginformation associated with this tag */
    21: optional TrackingInformation trackingInformation;

    /** The time at which this tag was last updated */
    22: optional i64 updatedAt;

    /** The currently active sponsorships for this tag */
    23: optional list<sponsorship.Sponsorship> activeSponsorships;

    /** the id of the sponsorship for a paid content tag, for internal use
    external consumers should only use the activeSponsorships data */
    25: optional i64 sponsorshipId;

    /** is this tag expired - only set to true on paid content tags */
    26: required bool expired;

    /** Any other paid content data for this tag */
    27: optional PaidContentInformation paidContentInformation;

    /** Any campaigninformation associated with this tag */
    28: optional CampaignInformation campaignInformation;

    /** Football crest image */
    29: optional image.Image footballCrest;
}
