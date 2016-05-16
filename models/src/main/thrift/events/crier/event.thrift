namespace scala com.gu.crier.model.event.v1

include "content/v1.thrift"

enum EventType {
    Update = 1,
    Delete = 2
}

enum ItemType {
    Content = 1,
    Tag = 2,
    Section = 3,
    StoryPackage = 4
}

union EventPayload {

  1: v1.Content content

}

struct Event {

    1: required string payloadId

    2: required EventType eventType

    3: required ItemType itemType

    4: required i64 dateTime

    5: optional EventPayload payload
}