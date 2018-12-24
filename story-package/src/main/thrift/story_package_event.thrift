namespace java com.gu.storypackage.model.v1
#@namespace scala com.gu.storypackage.model.v1

include "story_package_article.thrift"

/* The event type describe the resource state */
enum EventType {
    Update = 1,
    Delete = 2
}

struct Event {

    1: required EventType eventType;

    2: required string packageId;

    3: required string packageName;

    4: required string lastModified;

    5: required list<story_package_article.Article> articles;

}

