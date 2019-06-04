namespace scala com.gu.contentapi.client.model.v1

include "Podcast.thrift"
include "Reference.thrift"
include "Sponsorship.thrift"

enum TagType {

    CONTRIBUTOR = 0,

    KEYWORD = 1,

    SERIES = 2,

    NEWSPAPER_BOOK_SECTION = 3,

    NEWSPAPER_BOOK = 4,

    BLOG = 5,

    TONE = 6,

    TYPE = 7,

    PUBLICATION = 8,

    TRACKING = 9,

    PAID_CONTENT = 10,

    CAMPAIGN = 11

}

struct Tag {

    /*
     * The id of this tag: this should always be the path
     * to the tag page on www.theguardian.com
     */
    1: required string id

    /*
     * The type of this tag
     */
    2: required TagType type

    /*
     * Section is usually provided: some tags (notably contributor tags)
     * does not belong to any section so this will be None
     */
    3: optional string sectionId

    /*
     * The display name of the section.  Will be None if sectionId is None.
     */
    4: optional string sectionName

    /*
     * Short description of this tag.
     */
    5: required string webTitle

    /*
     * Full url on which tag page can be found on www.theguardian.com
     */
    6: required string webUrl

    /*
     * Full url on which full information about this tag can be found on
     * the content api.
     *
     * For tags, this allows access to the editorsPicks for the tag,
     * and automatically shows the most recent content for the tag.
     */
    7: required string apiUrl

    /*
     * List of references associated with the tag. References are
     * strings that identify things beyond the content api. A good example
     * is an isbn number, which associates the tag with a book.
     *
     * Use showReferences passing in the the type of reference you want to
     * see or 'all' to see all references.
     */
    8: required list<Reference.Reference> references

    /**
     * A tag *may* have a description field.
     *
     * Contributor tags never have a description field. They may
     * instead have a 'bio' field.
     */
    9: optional string description

    /**
     * If this tag is a contributor then we *may* have a small bio
     * for the contributor.
     *
     * This field is optional in all cases, even contributors are not
     * guaranteed to have one.
     */
    10: optional string bio

    /*
     * If this tag is a contributor then we *may* have a small byline
     * picturefor the contributor.
     *
     * This field is optional in all cases, even contributors are not
     * guaranteed to have one.
     */
    11: optional string bylineImageUrl

    /**
     * If this tag is a contributor then we *may* have a large byline
     * picture for the contributor.
     */
    12: optional string bylineLargeImageUrl

    /*
     * If this tag is a series it could be a podcast.
     */
    13: optional Podcast.Podcast podcast

    /*
     * If the tag is a contributor it may have a first name, a last name, email address and a twitter handle.
     */
    14: optional string firstName

    15: optional string lastName

    16: optional string emailAddress

    17: optional string twitterHandle

    /**
    * A list of all the active sponsorships running against this tag
    */
    18: optional list<Sponsorship.Sponsorship> activeSponsorships

    19: optional string paidContentType

    20: optional string paidContentCampaignColour

    21: optional string rcsId

    22: optional string r2ContributorId

    /*
     * A set of schema.org types, e.g. "Person", "Place"
     */
    23: optional set<string> tagCategories

    /*
     * A set of Guardian Entity IDs associated with this Tag
     */
    24: optional set<string> entityIds

    /**
    * If the tag is a campaign, it should have a subtype eg callout
    */
    25: optional string campaignInformationType

    /**
    * The internal name of the tag
    */
    26: optional string internalName
}
