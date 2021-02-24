namespace scala com.gu.crier.model.event.v1
#@namespace typescript _at_guardian.content_api_models.crier.event.v1

include "content/v1.thrift"
include "contentatom.thrift"

/*
 * For large update events the EventType is set to RetrievableUpdate
 * and the EventPayload.Content is replaced by an EventPayload.RetrievableContent
 */
enum EventType {

    Update = 1,
    Delete = 2,
    RetrievableUpdate = 3
}

enum ItemType {

    Content = 1,
    Tag = 2,
    Section = 3,
    StoryPackage = 4,
    Atom = 5
}

struct RetrievableContent {

    /*
     * The content id
     */
    1: required string id
    /*
     * An API link for the client to fetch the content
     */
    2: required string capiUrl

    /*
     * The timestamp for when that specific payload was last modified
     */
    3: optional i64 lastModifiedDate

    /*
     * The internal revision number of the replaced content payload
     */
    4: optional i32 internalRevision

    /*
     * The content type
     * Convenience field to help consumers filter events
     */
    5: optional v1.ContentType contentType

    /*
    * If the content's URL has evolved over time, we include
    * the aliasPaths so that e.g. de-caching can be comprehensively
    * triggered when the content is updated
    */

    /* NOT USED
     * aliasPaths is no longer a list<string>
     */
     //6: optional list<string> aliasPaths

    7: optional list<v1.AliasPath> aliasPaths
}

/*
* Until the advent of aliasPaths, we never needed to send a payload in delete events.
* The DeletedContent struct enables us to include aliasPaths as a new kind of payload
* and we decided that it would be sensible to express that in a future-expandable way
* rather than create a new payload type specifically and only for aliasPaths
 */
struct DeletedContent {

    /*
    * The aliases associated with evolved URLs
    */
    /* NOT USED
     * aliasPaths is no longer a list<string>>
     */
    //1: optional list<string> aliasPaths

    2: optional list<v1.AliasPath> aliasPaths
}

union EventPayload {

  1: v1.Content content

  2: RetrievableContent retrievableContent

  3: contentatom.Atom atom

  4: DeletedContent deletedContent
}

struct Event {

    1: required string payloadId

    2: required EventType eventType

    3: required ItemType itemType

    4: required i64 dateTime

    5: optional EventPayload payload
}


