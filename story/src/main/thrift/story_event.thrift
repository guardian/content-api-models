namespace java com.gu.story.model.v1
#@namespace scala com.gu.story.model.v1

include "story_model.thrift"

/* The event type describe the resource state */
enum EventType {
    Update = 1,
    Delete = 2
}

struct Event {

    1: required EventType eventType

    2: required story_model.Story story

}

