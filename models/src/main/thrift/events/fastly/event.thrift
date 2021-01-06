namespace scala com.gu.fastly.model.event.v1
#@namespace typescript _at_guardian.content_api_models.fastly.event.v1

include "content/v1.thrift"

enum EventType {
    Update = 1,
    Delete = 2,
}

struct ContentDecacheEvent {
    1: required string contentId

    2: required EventType eventType

   /*
    * The content type
    * Convenience field to help consumers filter events
    */
    3: optional v1.ContentType contentType

   /*
    * If the content's URL has evolved over time, we include
    * the aliasPaths so that e.g. de-caching can be comprehensively
    * triggered when the content is updated
    */
    4: optional list<string> aliasPaths
}
