namespace java  com.gu.contentatom.thrift
#@namespace scala com.gu.contentatom.thrift

/** date times are reprsented as i64 - epoch millis */
typedef i64 DateTime

typedef string OpaqueJson

struct User {
  1: required string email
  2: optional string firstName
  3: optional string lastName
}

struct ChangeRecord {

  /** when the change occured */
  1: required DateTime date

  /** the user that performed the change */
  2: optional User user

}

/**
* Represents a section.
*
* All tags exist within a section and the content's section is derived from the section of the most
* important tag.
*/
struct Section {

  /** The id of the section. This is derived from the R2 section id */
  1: required i64 id

  /** The section's name */
  2: optional string name

  /**
  * The path fragment implied by the section.
  *
  * content paths include {section.pathPrefix}/{tag.slug}/{date}/{content.slug}
  */
  3: optional string pathPrefix

  /** The url slug used when refering to the section */
  4: optional string slug

}

/**
* Represents a tag applied to content
*
* The id is the only required field, all the rest of the data can be looked up using the id.
*/
struct Tag {

  /** The id of the tag. This is the R2 id and can be used to look up the tag in the tagApi etc. */
  1: required i64 id

  /** The tag's type */
  2: optional string type

  /** The internal name of the tag */
  3: optional string internalName

  /** The external name of the tag */
  4: optional string externalName

  /** The path fragment associated with the tag */
  5: optional string slug

  /** The section the tag belongs to */
  6: optional Section section

  /** The full path of the tag */
  7: optional string path

}

/**
* Represents where the contant appears in the newspaper
*
* The book is the physical printed thing (G1, G2 etc), the bookSection is the subsection of the
* book (news, business, obituries etc.) and the publication is the physical publication(The Guardian, The Observer).
* This information, along with the newspaperPageNumber and newspaperPublicationDate fields,
* is used to produce the daily newspaper navigation pages.
*/
struct Newspaper {

  /** The book tag represents the physical printed thing (G1, G2 etc) the content appeared in */
  1: required Tag book

  /** bookSection represents the subsection of the book (news, business, obituries etc.) he content appeared in*/
  2: required Tag bookSection

  /** publication represents the physical publication the content has been printed in */
  3: required Tag publication

}

/**
* Represents a Tag's application to content.
*
* Includes the Tag and if the tag <-> content relationship is marked as lead
*/
struct TagUsage {

  /** The tag applied to content */
  1: required Tag tag

  /** true if the content is lead for this tag */
  2: required bool isLead = false

}

/**
* An external reference applied to content
*
* An external reference typically refers to a thing in another system or a real world thing.
* Examples include the isbn of a book the content is reviewing or a cricket match.
*/
struct Reference {

  /** The external id */
  1: required string id

  /** The type of reference */
  2: required string type

}

/**
* Taxonomy represents the tags and references of a piece of content
*/
struct Taxonomy {

  /** The list of tags applied to the content.
   *
   * The tags in this list are all the non contributor, newspaper and publication tags. They
   * are ordered by importance (most important first). Any tag in this list can be marked as 'lead'
   * marking this content as the most important story for the tag at the time of publication.
  */
  1: optional list<TagUsage> tags

  /** The list of contributor tags for this content.
   *
   * Contributors are managed via the content's byline and links to the contributor tag pages are
   * included in the byline
  */
  2: optional list<Tag> contributors

  /** The publication that commissioned this content*/
  3: optional Tag publication

  /** The newspaper book and book section that the content appeared in */
  4: optional Newspaper newspaper

  /** The external references applied to this content */
  5: optional list<Reference> references
}

struct ImageAssetDimensions {
  1: required i32 height

  2: required i32 width
}

struct ImageAsset {
  1: optional string mimeType

  2: required string file

  3: optional ImageAssetDimensions dimensions

  4: optional i64 size

  5: optional string aspectRatio

  6: optional string credit
  
  7: optional string copyright
  
  8: optional string source
  
  9: optional string photographer
  
  10: optional string suppliersReference
}

struct Image {
  1: required list<ImageAsset> assets

  2: optional ImageAsset master

  3: required string mediaId

  /** aka `credit` or `provider` in the IPTC metadata spec **/
  4: optional string source

  /** fullname of the image photographer **/
  5: optional string photographer

  /** description of image, used by screen readers **/
  6: optional string altText
}

/**
 * An abstraction to represent email subscriber lists
 */
struct EmailProvider {
  1: required string name = "exact-target"
  2: required string listId
}

/**
 * Reference to a third-party service used to send notifications
 */
struct NotificationProviders {
  1: optional EmailProvider email
}
