namespace scala com.gu.fastly.model.event.v1
#@namespace typescript _at_guardian.content_api_models.fastly.event.v1

include "content/v1.thrift"

enum EventType {
    Update = 1,
    Delete = 2,
}

struct ContentDecachedEvent {
    1: required string contentPath

    2: required EventType eventType

   /*
    * The content type
    * Convenience field to help consumers filter events
    */
    3: optional v1.ContentType contentType

   /*
    * Deprecated aliasPaths field.
    * Content items which decache multiple paths should issue multiple decache messages
    * Do not reuse this label number.
    * 4: optional list<string> aliasPaths
    */

   /*
    * When was this decache event published
    * Date times are represented as i64 - epoch millis
    */
    5: optional i64 published
}
